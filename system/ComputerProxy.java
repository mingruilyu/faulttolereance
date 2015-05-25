package system;

import java.rmi.RemoteException;

import space.SpaceImpl;

public class ComputerProxy {
	final private Computer computer;
	final private SpaceImpl space;
	final public int proxyId;
	public final int jobId;
	public ComputerProxy(SpaceImpl space, Computer computer, int proxyId, int jobId) {
		this.computer = computer;
		this.space = space;
		this.proxyId = proxyId;
		this.jobId = jobId;
	}
	
	public void startWorker() {
		try {
			int workerNo = this.computer.getWorkerNo();
			for(int i = 0; i < workerNo; i ++)
				new WorkerProxy(this.space, this.computer, this.proxyId, this.jobId).start();
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}
}