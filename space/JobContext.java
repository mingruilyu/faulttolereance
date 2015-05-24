package space;

import java.rmi.RemoteException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;

import system.ComputerProxy;
import api.Argument;
import api.Job;
import api.Task;

public class JobContext {
	private BlockingDeque<Task> readyQueue;
	private Map<Long, Task> waitingQueue;
	private BlockingQueue resultQueue;
	private BlockingQueue<Task> shadow;
	private long taskCounter;
	private Map<Integer, ComputerProxy> computerList;
	private Double shared;
	private Job job;
	
	public JobContext() {
		this.computerList = Collections.synchronizedMap(new HashMap<Integer, ComputerProxy>());
		this.readyQueue = new LinkedBlockingDeque<Task>();
		this.waitingQueue = Collections.synchronizedMap(new HashMap<Long, Task>());
		this.resultQueue = new LinkedBlockingQueue<Task>();
		this.shadow = new LinkedBlockingQueue<Task>();
		this.shared = (double) 100000;
	}
	
	public void setJob(Job job) {
		this.readyQueue.clear();
		this.waitingQueue.clear();
		this.resultQueue.clear();
		this.shadow.clear();
		this.job = job;
	}
	
	public void addComputer(ComputerProxy computerProxy, int computerCount) {
		this.computerList.put(computerCount, computerProxy);
	}
	
	public <T> Task<T> fetchTask() throws InterruptedException  {
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
			this.readyQueue.put(this.job.toTask());
		} catch (InterruptedException e) {
			e.printStackTrace();
		}		
	}
	
	public <T> void suspendTask(Task<T> task, long id) throws RemoteException {
		this.waitingQueue.put(id, task);
	}
	
	synchronized public long getTaskId() throws RemoteException {
		return this.taskCounter ++;
	}
	
	public Double getShared() throws RemoteException {
		return this.shared;
	}
	
	synchronized public void putShared(Double shared) throws RemoteException {
		if(this.shared > shared)
			this.shared = shared;
	}
}
