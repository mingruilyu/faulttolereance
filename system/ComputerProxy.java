package system;

import java.rmi.RemoteException;

import space.SpaceImpl;

public class ComputerProxy {
	final private Computer computer;
	final private SpaceImpl space;
	public final int jobId;
	private final Object lock;
	public ComputerProxy(SpaceImpl space, Computer computer, int jobId, Object lock) {
		this.computer = computer;
		this.space = space;
		this.jobId = jobId;
		this.lock = lock;
	}
	
	public void startWorker() {
		try {
			int workerNo = this.computer.getWorkerNo();
			for(int i = 0; i < workerNo; i ++)
				new WorkerProxy(this.space, this.computer, this.jobId, this.lock).start();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}