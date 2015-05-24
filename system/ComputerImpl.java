package system;

import java.rmi.Naming;
import java.rmi.RemoteException;
import java.rmi.server.UnicastRemoteObject;

import api.Space;
import api.Task;

public class ComputerImpl extends UnicastRemoteObject implements Computer {
	public static final long serialVersionUID = 227L; 	
	private long startTime;
	private long endTime;
	private int workerNo;
	public static final String MULTITHREAD_ON = "MT_ON";
	public static final String MULTITHREAD_OFF = "MT_OFF";
	public ComputerImpl(boolean multithreadFlag) throws RemoteException {
		super();
		if(multithreadFlag)
			this.workerNo = Runtime.getRuntime().availableProcessors();
		else this.workerNo = 1;
	}
	
	public static void main(String[] args) {
		if(System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		Computer computer;

		try {
			String url = "rmi://" + args[0] + ":" + Space.PORT + "/"
					+ Space.SERVICE_NAME;
			Space space = (Space) Naming.lookup(url);
			if (args[1].equals(ComputerImpl.MULTITHREAD_ON)) {
				computer = new ComputerImpl(true);
				System.out.println("Multithread is on");
			} else {
				computer = new ComputerImpl(false);
				System.out.println("Multithread is off");
			}
			space.register(computer);
			
		} catch (Exception e) {
			System.out.println("ComputeEngine Exception");
			e.printStackTrace();
		}
		System.out.println("Computer is running");
	}
	
	@Override
	public void exit() throws RemoteException {			
		System.exit(0);
	}

	@Override
	public <T> long executeTask(Task<T> task, Space space) throws RemoteException {
		this.startTime = System.nanoTime();
		task.run(space);
		this.endTime = System.nanoTime();
		return this.endTime - this.startTime;
	}

	@Override
	public int getWorkerNo() throws RemoteException {
		return this.workerNo;
	}
	
	@Override
	synchronized public void decrementWorkerNo() throws RemoteException {
		this.workerNo --;
	}
}
