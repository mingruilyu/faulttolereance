package system;

import java.rmi.Remote;
import java.rmi.RemoteException;

import api.Space;
import api.Task;

public interface Computer extends Remote {
	<T> long executeTask(Task<T> t, Space space) throws RemoteException;
	int getWorkerNo() throws RemoteException;
	void exit() throws RemoteException;
	void decrementWorkerNo() throws RemoteException;
}
