package api;

import java.io.Serializable;
import java.rmi.RemoteException;


public class Argument<V> implements Serializable {
	V argument;
	long time;
	
	public V getArg() throws RemoteException{
		return this.argument;
	}

	public long getTime() throws RemoteException{
		return this.time;
	}

	public Argument(V argument, long time){
		this.argument = argument;
		this.time = time;	
	}
}
