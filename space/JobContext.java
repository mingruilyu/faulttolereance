package space;

import java.io.Serializable;
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
	private SpaceImpl space;
	
	public static final long serialVersionUID = 227L;
	
	public JobContext(SpaceImpl space) {
		this.computerList = Collections.synchronizedMap(new HashMap<Integer, Computer>());
		this.readyQueue = new LinkedBlockingDeque<Task>();
		this.waitingQueue = Collections.synchronizedMap(new HashMap<Long, Task>());
		this.resultQueue = new LinkedBlockingQueue<Task>();
		this.shadow = Collections.synchronizedMap(new HashMap<Long, Task>());
		this.shared = (double) 100000;
		this.space = space;
		this.taskCounter = 0;
	}
	
	public void setJob(Job job) {
		this.readyQueue.clear();
		this.waitingQueue.clear();
		this.resultQueue.clear();
		this.shadow.clear();
		this.job = job;
	}
	
	public void addComputer(Computer computer, int computerCount) {
		this.computerList.put(computerCount, computer);
	}
	
	public <T> Task<T> fetchTask(boolean mode) throws InterruptedException  {
		Task<T> task = this.readyQueue.getLast();
		if(mode == SpaceImpl.MODE_SPACE)
			this.shadow.put(task.taskId, task);
		return this.readyQueue.takeLast();
	}
	
	public <T> void issueTask(Task<T> task) {
		try {
			this.readyQueue.put(task);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	public <T> void insertArg(Argument<T> arg, long id, int slotIndex) {
		Task task = this.waitingQueue.get(id);	
		synchronized(task) {
			task.insertArg(arg, slotIndex);	
			if(task.isReady()) {
				this.waitingQueue.remove(id);
				this.issueTask(task);
			}
		}
	}
	
	public <T> T take() throws InterruptedException{
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
		try {
			this.readyQueue.put(this.job.toTask(this.taskCounter ++));
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	public <T> void suspendTask(Task<T> task, long taskId, boolean mode) {
		this.waitingQueue.put(taskId, task);
		if(mode == SpaceImpl.MODE_SPACE)
			this.shadow.remove(taskId, task);
	}
	
	synchronized public long getTaskId() {
		return this.taskCounter ++;
	}
	
	public Double getShared() {
		return this.shared;
	}
	
	synchronized public void putShared(Double shared) {
		if(this.shared > shared)
			this.shared = shared;
	}
	
	void removeDuplicate() {
		// remove the duplicate between ready queue and shadow
		Task task = this.readyQueue.getLast();
		if(this.shadow.containsKey(task.taskId))
			this.shadow.remove(task.taskId);
		// remove the duplicate between waiting queue and shadow
		for(Long taskId : this.shadow.keySet()) {
			if(this.waitingQueue.containsKey(taskId)) {
				this.waitingQueue.remove(taskId);
				break;
			}
		}
	} 
	
	void resumeJob() {
		for(Integer computerId : this.computerList.keySet()) {
			Computer computer = this.computerList.get(computerId);
			ComputerProxy computerProxy = new ComputerProxy(this.space, computer, computerId, this.jobId);
			computerProxy.startWorker();
		}
	}
}
