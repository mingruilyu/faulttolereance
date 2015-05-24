package space;

import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import system.Computer;
import system.ComputerProxy;
import api.Argument;
import api.Job;
import api.Space;
import api.Task;

public class SpaceImpl extends UnicastRemoteObject implements Space {
	private BlockingDeque<Task> taskQueue;
	private Map<Long, Task> waitingQueue;
	private BlockingQueue resultQueue;
	private Map<Integer, ComputerProxy> computerList;
	private long taskCounter;
	private Double shared;
	private final static String RUNNABLE_ON = "SR_ON";
	private final static String RUNNABLE_OFF = "SR_OFF";
	
	public SpaceImpl()  throws RemoteException {
		this.taskQueue = new LinkedBlockingDeque<Task>();
		this.computerList = Collections.synchronizedMap(new HashMap<Integer, ComputerProxy>());
		this.waitingQueue = Collections.synchronizedMap(new HashMap<Long, Task>());
		this.resultQueue = new LinkedBlockingQueue<Task>();
		this.shared = (double) 100000;
	}

	@Override
	public void register(Computer computer) throws RemoteException {
		ComputerProxy computerProxy = new ComputerProxy(this, computer, this.computerList.size());
		this.computerList.put(this.computerList.size(), computerProxy);
		computerProxy.startWorker();
	}

	@Override
	public <T> Task<T> fetchTask() throws RemoteException, InterruptedException {
		return this.taskQueue.takeLast();
	}
	
	synchronized public void deleteComputerProxy(int proxyId) {
		this.computerList.remove(proxyId);
	}

	public static void main(String[] args) throws RemoteException, NotBoundException {
		Space space = null;
		if(System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		try {
			space = new SpaceImpl();
			SpaceWorker spaceWorker = new SpaceWorker(space);
			Registry registry = LocateRegistry.createRegistry(Space.PORT);
			registry.rebind(Space.SERVICE_NAME, space);
			System.out.println("Space in on, waiting for connection ...");
			if(args[0].equals(SpaceImpl.RUNNABLE_ON)) {
				spaceWorker.start();
				System.out.println("Space Runnable is on");
            } else System.out.println("Space Runnable is off");
		} catch (Exception e) {
			System.out.println("Space Exception");
			e.printStackTrace();
		}
	}

	@Override
	public <T> void issueTask(Task<T> task) throws RemoteException {
		try {
			this.taskQueue.put(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	@Override
	public <T> void insertArg(Argument<T> arg, long id, int slotIndex)
			throws RemoteException {
		Task task = this.waitingQueue.get(id);	
		synchronized(task) {
			task.insertArg(arg, slotIndex);	
			if(task.isReady()) {
				this.waitingQueue.remove(id);
				this.issueTask(task);
			}
		}
	}

	@Override
	public <T> T take() throws RemoteException, InterruptedException {
		return (T) resultQueue.take();
	}

	@Override
	public <T> void setupResult(T result) throws RemoteException, InterruptedException {
		this.resultQueue.put(result);
	}

	@Override
	public <T> void startJob(Job<T> job) throws RemoteException,
			InterruptedException {
		this.taskQueue.put(job.toTask(this));		
	}

	@Override
	public <T> void suspendTask(Task<T> task, long id) throws RemoteException {
		this.waitingQueue.put(id, task);
	}

	@Override
	synchronized public long getTaskId() throws RemoteException {
		return this.taskCounter ++;
	}

	@Override
	public Double getShared() throws RemoteException {
		return this.shared;
	}

	@Override
	synchronized public void putShared(Double shared) throws RemoteException {
		if(this.shared > shared)
			this.shared = shared;
	}
}
