package space;

import java.rmi.RemoteException;
import api.Space;

public class SpaceWorker extends Thread{
	private Space space;

	public SpaceWorker(Space space) {
		this.space = space;
	}
	@Override
	public void run() {
		super.run();
		while(true){
			try {
				this.space.fetchTask().run(this.space);
			} catch (RemoteException | InterruptedException e) {
				e.printStackTrace();
				System.out.println("Space Worker Exception");
			}	
		}
	}
}
