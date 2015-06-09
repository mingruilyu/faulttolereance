package system;

import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CompManagerImpl extends UnicastRemoteObject implements CompManager{
	private Map<Computer, Boolean> computerList;
	private int unusedComputer;
	
	
	public CompManagerImpl() throws RemoteException{
		this.computerList = new HashMap<Computer, Boolean>();
		unusedComputer = 0;
	}
	public void register(Computer computer) throws RemoteException {
		this.computerList.put(computer, false);
		this.unusedComputer++;
	}

	public List<Computer> allocateComputer(int num) throws RemoteException{
		if (num > this.unusedComputer)
			return null;
		List<Computer> allocateList = new ArrayList<Computer>();
		int addCount = 0;
		synchronized (computerList) {
			for (Computer computer : computerList.keySet()) {
				if (addCount == num)
					break;
//				addCount++;
				if (!computerList.get(computer)) {	//addCount++ only when the computer can be used
					allocateList.add(computer);
					addCount++;
					computerList.put(computer, true);
					unusedComputer--;
				}
			}
		}
		return allocateList;
	}

	public void releaseComputer(List<Computer> compList) throws RemoteException{
		synchronized (compList) {
			for (Computer comp : compList) {
				computerList.put(comp, false);
				unusedComputer++;
			}
			System.out.println("Release Computer!");
		}
	}
	
	public static void main(String[] args) throws RemoteException{
		CompManager compManager = null;
		if(System.getSecurityManager() == null)
			System.setSecurityManager(new SecurityManager());
		try {
			compManager = new CompManagerImpl();
			Registry registry = LocateRegistry.createRegistry(CompManager.PORT);
			registry.rebind(CompManager.SERVICE_NAME, compManager);
			System.out.println("Manager in on, waiting for connection ...");
		} catch (Exception e) {
			System.out.println("Manager Exception");
			e.printStackTrace();
		}
	}
}
