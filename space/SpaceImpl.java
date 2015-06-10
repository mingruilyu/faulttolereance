package space;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import system.Computer;
import api.Argument;
import api.CompManager;
import api.Job;
import api.JobContext;
import api.Shared;
import api.Space;
import api.Task;

public class SpaceImpl extends UnicastRemoteObject implements Space {
	private Map<Integer, JobContext> jobContextMap;
	private Space mirror;
	private CompManager compManager;

	Map<Integer, CheckPointTimer> timerMap;

	private final static String RUNNABLE_ON = "SR_ON";
	private final static String RUNNABLE_OFF = "SR_OFF";
	final static int MAX_JOB_NO = 2;
	private final static String MODE_SPACE_STR = "SPACE";
	private final static String MODE_MIRROR_STR = "MIRROR";
	public final static boolean MODE_SPACE = true;
	public final static boolean MODE_MIRROR = false;
	public final boolean mode;
	private int jobCount;

	public SpaceImpl(boolean mode, String managerHostname)
			throws RemoteException, MalformedURLException, NotBoundException {
		this.jobCount = 0;
		this.mode = mode;
		this.jobContextMap = new HashMap<Integer, JobContext>();
		if (mode == MODE_SPACE) {
			for (int i = 0; i < MAX_JOB_NO; i++)
				this.jobContextMap.put(i, new JobContext(this));
		}
		this.timerMap = new HashMap<Integer, CheckPointTimer>();
		String url = "rmi://" + managerHostname + ":" + CompManager.PORT + "/"
				+ CompManager.SERVICE_NAME;
		compManager = (CompManager) Naming.lookup(url);
	}

	// @Override
	// public int register(Computer computer) throws RemoteException {
	// int jobId = this.computerCount % MAX_JOB_NO;
	// JobContext jobContext = this.jobContextMap.get(jobId);
	// jobContext.addComputer(computer, this.computerCount, this);
	// computerCount++;
	// return jobId;
	// }

	@Override
	public <T> Task<T> fetchTask(int jobId) throws RemoteException,
			InterruptedException {
		return this.jobContextMap.get(jobId).fetchTask(this.mode);
	}

	/*
	 * synchronized public void deleteComputerProxy(int proxyId) {
	 * this.computerList.remove(proxyId); }
	 */

	public static void main(String[] args) throws RemoteException,
			NotBoundException {
		if (System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		try {
			if (args[0].equals(SpaceImpl.MODE_MIRROR_STR)) {
				Space mirror = new SpaceImpl(SpaceImpl.MODE_MIRROR, args[2]);
				String url = "rmi://" + args[1] + ":" + Space.PORT + "/"
						+ Space.SERVICE_NAME;
				Space space = (Space) Naming.lookup(url);
				space.addMirror(mirror);
				System.out.println("Mirror is set, ready for crash ...");

			} else {
				Space space = new SpaceImpl(SpaceImpl.MODE_SPACE, args[1]);
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
	public <T> T takeFinalResult(int jobId) throws RemoteException, InterruptedException {
		return this.jobContextMap.get(jobId).takeFinalResult();
	}
	@Override
	public <T> T takeIntermediateResult(int jobId) throws RemoteException, InterruptedException {
		return this.jobContextMap.get(jobId).takeIntermediateResult();
	}
	
	@Override
	public <T> void setupFinalResult(boolean isFinal, T result, int jobId) throws RemoteException {
		JobContext jobContext = this.jobContextMap.get(jobId);
		jobContext.setupFinalResult(result);
		compManager.releaseComputer(jobContext.computerList);
	}

	@Override
	public <T> void startJob(int jobId) throws RemoteException {
		System.out.println("Get jobContext "+jobId);
		JobContext jobContext = this.jobContextMap.get(jobId);
		jobContext.startJob();
		this.timerMap.put(jobId, new CheckPointTimer(jobContext, this.mirror,
				jobId));
	}

	@Override
	public <T> void suspendTask(Task<T> task, int jobId) throws RemoteException {
		this.jobContextMap.get(jobId).suspendTask(task, task.taskId, this.mode);
	}

	@Override
	public <T> void clearShadow(Task<T> task, int jobId) throws RemoteException {
		this.jobContextMap.get(jobId).clearShadow(task, task.taskId, this.mode);
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
	synchronized public void putShared(Shared shared, int jobId)
			throws RemoteException {
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
	public int prepareJob(Job job, int compNum) throws RemoteException {
		System.out.println("JobCount: "+jobCount);
		int jobId = (this.jobCount++) % MAX_JOB_NO;
		System.out.println("jobId: "+jobId);
		job.setJobId(jobId);
		this.jobContextMap.get(jobId).setJob(job);
		JobContext jobContext = this.jobContextMap.get(jobId);
		List<Computer> computerList = compManager.allocateComputer(compNum);
		if(computerList == null) {
			System.out.println("Currently no computer available!");
			return -1;
		}
		for (Computer computer : computerList)
			jobContext.addComputer(computer, this, jobId);
		return jobId;
	}

	public void supplementComputer(int jobId) {
		JobContext jobContext = this.jobContextMap.get(jobId);
		System.out.println("Supple a new computer!");
		try {
			List<Computer> computerList = compManager.allocateComputer(1);
			if(computerList == null) return;
			jobContext.addComputer(computerList.get(0), this, jobId);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
	
	@Override
	public void checkPoint(JobContext jobContext, int jobId)
			throws RemoteException {
		jobContext.removeDuplicate();
		Iterator<Long> it = jobContext.shadow.keySet().iterator();
		while (it.hasNext()) {
			Long key = it.next();
			try {
				jobContext.readyQueue.put(jobContext.shadow.remove(key));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		this.jobContextMap.put(jobId, jobContext);
	}

	public void removeComputerRequest(Computer computer) throws RemoteException {
		this.compManager.removeComputer(computer);
	}
	
	@Override
	public void resumeJob(int jobId) throws RemoteException {
		this.jobContextMap.get(jobId).resumeJob(this);
	}

	@Override
	public void synchronizeFinalResult(int jobId) throws RemoteException {
		this.jobContextMap.get(jobId).putShared();
	}
}
