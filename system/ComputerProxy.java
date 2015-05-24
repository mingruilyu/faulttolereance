package system;

import java.rmi.RemoteException;

import space.SpaceImpl;

public class ComputerProxy {
	final private Computer computer;
	final private SpaceImpl space;
	final public int id;
	public ComputerProxy(SpaceImpl space, Computer computer, int id) {
		this.computer = computer;
		this.space = space;
		this.id = id;
	}
	
	public void startWorker() {
		try {
			int workerNo = this.computer.getWorkerNo();
			for(int i = 0; i < workerNo; i ++)
				new WorkerProxy(this.space, this.computer, this.id).start();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}