package system;

import java.rmi.RemoteException;

import space.SpaceImpl;
import api.Task;

public class WorkerProxy extends Thread {
	private boolean running;
	private SpaceImpl space;
	private Computer computer;
	private int jobId;
	private Object lock;
	public WorkerProxy(SpaceImpl space, Computer computer, int jobId, Object lock) {
		this.space = space;
		this.computer = computer;
		this.jobId = jobId;
		this.lock = lock;
	}
	
	@Override
	public void run() {
		super.run();
		System.out.println("Start a new worker proxy!");
		this.running = true;
		System.out.println("Current job id: "+jobId);
		while(this.running) {
			Task task = null;
			try {
				task = this.space.fetchTask(this.jobId);
				if(task == null){
					synchronized (this.lock) {
						System.out.println("waiting for..." + jobId);
						this.lock.wait();
						//////////////////////
						continue;	
						//////////////////////
					}	
				}
				long time = this.computer.executeTask(task, this.space);
//				System.out.println("Task running time: " + time);
			} catch (RemoteException | InterruptedException e) {
				e.printStackTrace();
				try {
					this.space.issueTask(task, this.jobId);
					//this.computer.decrementWorkerNo();
					//if(this.computer.getWorkerNo() == 0)
						//this.space.deleteComputerProxy(this.computerProxyId);
					break;
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				this.running = false;
			}
		}
	}
}
