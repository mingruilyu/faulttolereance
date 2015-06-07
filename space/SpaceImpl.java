package space;

import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Map;

import system.Computer;
import system.ComputerProxy;
import api.Argument;
import api.Job;
import api.Space;
import api.Task;

public class SpaceImpl extends UnicastRemoteObject implements Space {
	private Map<Integer, JobContext> jobContextMap;
	private Space mirror;
	
	Map<Integer, CheckPointTimer> timerMap;
	
	private final static String RUNNABLE_ON = "SR_ON";
	private final static String RUNNABLE_OFF = "SR_OFF";
	final static int MAX_JOB_NO = 2;
	private final static String MODE_SPACE_STR = "SPACE";
	private final static String MODE_MIRROR_STR = "MIRROR";
	public final static boolean MODE_SPACE = true;
	public final static boolean MODE_MIRROR = false;
	private final boolean mode;
	private int computerCount;
	private int jobCount;
	
	public SpaceImpl(boolean mode)  throws RemoteException {
		this.computerCount = 0;
		this.jobCount = 0;
		this.mode = mode;
		this.jobContextMap = new HashMap<Integer, JobContext>();
		if(mode == this.MODE_SPACE) {
			for(int i = 0; i < MAX_JOB_NO; i ++)
				this.jobContextMap.put(i, new JobContext(this));
		}
		this.timerMap = new HashMap<Integer, CheckPointTimer>();
	}

	@Override
	public int register(Computer computer) throws RemoteException {
		int jobId = this.computerCount % MAX_JOB_NO;
		JobContext jobContext = this.jobContextMap.get(jobId);
		jobContext.addComputer(computer, this.computerCount);
		computerCount++;
		return jobId;
	}

	@Override
	public <T> Task<T> fetchTask(int jobId) throws RemoteException, InterruptedException {
		return this.jobContextMap.get(jobId).fetchTask(this.mode);
	}
	
	/*synchronized public void deleteComputerProxy(int proxyId) {
		this.computerList.remove(proxyId);
	}*/

	public static void main(String[] args) throws RemoteException, NotBoundException {
		if(System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		try {
			if(args[0].equals(SpaceImpl.MODE_MIRROR_STR)) {
				Space mirror = new SpaceImpl(SpaceImpl.MODE_MIRROR);
				String url = "rmi://" + args[1] + ":" + Space.PORT + "/"
						+ Space.SERVICE_NAME;
				Space space = (Space) Naming.lookup(url);
				space.addMirror(mirror);
				System.out.println("Mirror is set, ready for crash ...");
			} else {
				Space space = new SpaceImpl(SpaceImpl.MODE_SPACE);
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
		JobContext jobContext = this.jobContextMap.get(jobId);
		jobContext.startJob();
		this.timerMap.put(jobId, new CheckPointTimer(jobContext, this.mirror, jobId));
	}

	@Override
	public <T> void suspendTask(Task<T> task, int jobId) throws RemoteException {
		this.jobContextMap.get(jobId).suspendTask(task, task.taskId, this.mode);
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
		job.setJobId(jobId);
		this.jobContextMap.get(jobId).setJob(job);
		return jobId;
	}

	@Override
	public void checkPoint(JobContext jobContext, int jobId) throws RemoteException {
		jobContext.removeDuplicate();
		for(Long taskId : jobContext.shadow.keySet()) {
			try {
				jobContext.readyQueue.put(jobContext.shadow.remove(taskId));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.jobContextMap.put(jobId, jobContext);
	}

	@Override
	public void resumeJob(int jobId) throws RemoteException {
		this.jobContextMap.get(jobId).resumeJob();
	}
}
