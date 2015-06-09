package api;

import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import space.SpaceImpl;
import system.Computer;
import system.ComputerProxy;

public class JobContext implements Serializable {
	public final BlockingDeque<Task> readyQueue;
	public final Map<Long, Task> waitingQueue;
	public final Map<Long, Task> shadow;
	public final BlockingQueue resultQueue;
	private long taskCounter;
	private Map<Integer, Computer> computerList;
	private Double shared;
	private Job job;
	private int jobId;
	private Lock lock;
	public static final long serialVersionUID = 227L;

	public JobContext(SpaceImpl space) {
		this.computerList = Collections
				.synchronizedMap(new HashMap<Integer, Computer>());
		this.readyQueue = new LinkedBlockingDeque<Task>();
		this.waitingQueue = Collections
				.synchronizedMap(new HashMap<Long, Task>());
		this.resultQueue = new LinkedBlockingQueue<Task>();
//		this.shadow = Collections.synchronizedMap(new HashMap<Long, Task>());
		this.shadow = new ConcurrentHashMap<Long, Task>();
		this.shared = (double) 100000;
		this.taskCounter = 0;
		this.lock = new Lock();
	}

	public void setJob(Job job) {
		this.readyQueue.clear();
		this.waitingQueue.clear();
		this.resultQueue.clear();
		this.shadow.clear();
		this.job = job;
	}

	public void addComputer(Computer computer, int computerCount,
			SpaceImpl space, int jobId) {
		this.jobId = jobId;
		this.computerList.put(computerCount, computer);
		ComputerProxy computerProxy = new ComputerProxy(space, computer,
				computerCount, this.jobId, this.lock);
		computerProxy.startWorker();
	}

	public <T> Task<T> fetchTask(boolean mode) throws InterruptedException {
		synchronized (readyQueue) {
			if (!this.readyQueue.isEmpty()) {
				Task<T> task = this.readyQueue.getLast();
				if (mode == SpaceImpl.MODE_SPACE)
					this.shadow.put(task.taskId, task);
				return this.readyQueue.takeLast();
			}
			return null;
		}

	}

	public <T> void issueTask(Task<T> task) {
		try {
			this.readyQueue.put(task);
			synchronized (lock) {
				this.lock.notifyAll();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public <T> void insertArg(Argument<T> arg, long id, int slotIndex) {
		Task task = this.waitingQueue.get(id);
		if(task == null) return;
		synchronized (task) {
			task.insertArg(arg, slotIndex);
			if (task.isReady()) {
				this.waitingQueue.remove(id);
				this.issueTask(task);
			}
		}
	}

	public <T> T take() throws InterruptedException {
		return (T) this.resultQueue.take();
	}

	public <T> void setupResult(T result) {
		try {
			this.resultQueue.put(result);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}

	public <T> void startJob() {
		this.issueTask(this.job.toTask(this.taskCounter++));
	}

	public <T> void suspendTask(Task<T> task, long taskId, boolean mode) {
		this.waitingQueue.put(taskId, task);
//		if (mode == SpaceImpl.MODE_SPACE)
//		this.shadow.remove(taskId, task);
	}
	
	public <T> void clearShadow(Task<T> task, long taskId, boolean mode){
		if (mode == SpaceImpl.MODE_SPACE)
		this.shadow.remove(taskId, task);
	}

	synchronized public long getTaskId() {
		return this.taskCounter++;
	}

	public Double getShared() {
		return this.shared;
	}

	synchronized public void putShared(Double shared) {
		if (this.shared > shared)
			this.shared = shared;
	}

	public void removeDuplicate() {
		if (!this.readyQueue.isEmpty()) {
			// remove the duplicate between ready queue and shadow
			Task task = this.readyQueue.getLast();
			if (this.shadow.containsKey(task.taskId))
				this.shadow.remove(task.taskId);
			// remove the duplicate between waiting queue and shadow
			Iterator<Long> it = this.shadow.keySet().iterator();
			while(it.hasNext()) {
				Long key = it.next();
				if (this.waitingQueue.containsKey(key)) {
					this.shadow.remove(key);
					break;
				}
			}
		}
	}

	public void resumeJob(SpaceImpl space) {
		for(Long key:this.shadow.keySet()){
			try {
				this.readyQueue.put(this.shadow.get(key));
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		for (Integer computerId : this.computerList.keySet()) {
			Computer computer = this.computerList.get(computerId);
			ComputerProxy computerProxy = new ComputerProxy(space, computer,
					computerId, this.jobId, this.lock);
			computerProxy.startWorker();
		}
	}

	class Lock implements Serializable {
		private static final long serialVersionUID = 1L;

		public Lock() {}
	}
}