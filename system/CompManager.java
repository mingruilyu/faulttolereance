package system;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

public interface CompManager extends Remote{
	public static int PORT = 8006;
	public static String SERVICE_NAME = "Manager";

	public void register(Computer computer) throws RemoteException;
	public List<Computer> allocateComputer(int num) throws RemoteException;
	public void releaseComputer(List<Computer> compList) throws RemoteException;
	
}
