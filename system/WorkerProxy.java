package system;

import java.rmi.RemoteException;

import space.SpaceImpl;
import api.Task;

public class WorkerProxy extends Thread {
	private boolean running;
	private SpaceImpl space;
	private Computer computer;
	private int computerProxyId;
	public WorkerProxy(SpaceImpl space, Computer computer, int computerProxyId) {
		this.computer = computer;
		this.space = space;
		this.computerProxyId = computerProxyId;
	}
	
	@Override
	public void run() {
		super.run();
		System.out.println("Start a new worker proxy!");
		this.running = true;
		while(this.running) {
			Task task = null;
			try {
				task = this.space.fetchTask();
				long time = this.computer.executeTask(task, this.space);
				System.out.println("Task running time: " + time);
			} catch (RemoteException | InterruptedException e) {
				e.printStackTrace();
				try {
					this.space.issueTask(task);
					this.computer.decrementWorkerNo();
					if(this.computer.getWorkerNo() == 0)
						this.space.deleteComputerProxy(this.computerProxyId);
					break;
				} catch (RemoteException e1) {
					e1.printStackTrace();
				}
				this.running = false;
			}
		}
	}
}
