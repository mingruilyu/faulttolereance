package space;

import java.rmi.Naming;
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
	private Map<Integer, JobContext> jobContextMap;
	private Map<Integer, JobContext> backupJobContextMap;
	
	private Space mirror;
	
	private final static String RUNNABLE_ON = "SR_ON";
	private final static String RUNNABLE_OFF = "SR_OFF";
	private final static int MAX_JOB_NO = 2;
	private final static String MODE_SPACE = "SPACE";
	private final static String MODE_MIRROR = "MIRROR";
	private int computerCount;
	private int jobCount;
	
	public SpaceImpl()  throws RemoteException {
		this.computerCount = 0;
		this.jobCount = 0;
		for(int i = 0; i < MAX_JOB_NO; i ++)
			jobContextMap.put(i, new JobContext());
	}

	@Override
	public int register(Computer computer) throws RemoteException {
		ComputerProxy computerProxy = new ComputerProxy(this, computer, this.computerCount);
		int jobId = this.computerCount % MAX_JOB_NO;
		JobContext jobContext = this.jobContextMap.get(jobId);
		jobContext.addComputer(computerProxy, this.computerCount ++);
		computerProxy.startWorker();
		return jobId;
	}

	@Override
	public <T> Task<T> fetchTask(int jobId) throws RemoteException, InterruptedException {
		return this.jobContextMap.get(jobId).fetchTask();
	}
	
	/*synchronized public void deleteComputerProxy(int proxyId) {
		this.computerList.remove(proxyId);
	}*/

	public static void main(String[] args) throws RemoteException, NotBoundException {
		if(System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		try {
			if(args[0].equals(SpaceImpl.MODE_MIRROR)) {
				Space mirror = new SpaceImpl();
				String url = "rmi://" + args[1] + ":" + Space.PORT + "/"
						+ Space.SERVICE_NAME;
				Space space = (Space) Naming.lookup(url);
				space.addMirror(mirror);
				System.out.println("Mirror is set, ready for crash ...");
			} else {
				Space space = new SpaceImpl();
				Registry registry = LocateRegistry.createRegistry(Space.PORT);
				registry.rebind(Space.SERVICE_NAME, space);
				System.out.println("Space is on, waiting for connection ...");
			}
			
		} catch (Exception e) {
			System.out.println("Space Exception");
			e.printStackTrace();
		}
	}

	@Override
	public <T> void issueTask(Task<T> task, int jobId) throws RemoteException {
		this.jobContextMap.get(jobId).issueTask(task);
	}

	@Override
	public <T> void insertArg(Argument<T> arg, long id, int slotIndex, int jobId)
			throws RemoteException {
		this.jobContextMap.get(jobId).insertArg(arg, id, slotIndex);
	}

	@Override
	public <T> T take(int jobId) throws RemoteException, InterruptedException {
		return this.jobContextMap.get(jobId).take();
	}

	@Override
	public <T> void setupResult(T result, int jobId) throws RemoteException {
		this.jobContextMap.get(jobId).setupResult(result);
	}

	@Override
	public <T> void startJob(int jobId) throws RemoteException {
		this.jobContextMap.get(jobId).startJob();		
	}

	@Override
	public <T> void suspendTask(Task<T> task, long id, int jobId) throws RemoteException {
		this.jobContextMap.get(jobId).suspendTask(task, id);
	}

	@Override
	synchronized public long getTaskId(int jobId) throws RemoteException {
		return this.jobContextMap.get(jobId).getTaskId();
	}

	@Override
	public Double getShared(int jobId) throws RemoteException {
		return this.jobContextMap.get(jobId).getShared();
	}

	@Override
	synchronized public void putShared(Double shared, int jobId) throws RemoteException {
		this.jobContextMap.get(jobId).putShared(shared);
	}

	@Override
	public void addMirror(Space mirror) throws RemoteException {
		this.mirror = mirror;
	}

	@Override
	public Space getMirror() throws RemoteException {
		return this.mirror;
	}

	@Override
	public int prepareJob(Job job) throws RemoteException {
		int jobId = (this.jobCount ++) % MAX_JOB_NO;
		this.jobContextMap.get(jobId).setJob(job);
		return jobId;
	}

	@Override
	public void resumeJob(int jobId) throws RemoteException {
		
	}
}
