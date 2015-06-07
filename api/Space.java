package api;

import java.rmi.Remote;
import java.rmi.RemoteException;
import java.util.List;

import space.JobContext;
import system.Computer;

/**
 * 
 * @author Gongxl
 *
 */
public interface Space extends Remote {
	public static int PORT = 8001;
	public static String SERVICE_NAME = "Space";

	/**
	 * Take the final result to the root task from the result queue.
	 * 
	 * @return the returned result.
	 * @throws RemoteException
	 *             occurs if there is a communication problem or the remote
	 *             service is not responding.
	 * @throws InterruptedException
	 *             occurs when a thread is waiting, sleeping, or otherwise
	 *             occupied, and the thread is interrupted, either before or
	 *             during the activity.
	 */
	<T> T take(int jobId) throws RemoteException, InterruptedException;

	/**
	 * Register the computer to the space.
	 * 
	 * @param computer
	 *            a remote reference to a Computer that is requesting.
	 * @throws RemoteException
	 *             occurs if there is a communication problem or the remote
	 *             service is not responding.
	 */
	int register(Computer computer) throws RemoteException;

	/**
	 * Put the generated new task to the task queue.
	 * 
	 * @param task
	 *            the task to be put into the task queue.
	 * @throws RemoteException
	 *             occurs if there is a communication problem or the remote
	 *             service is not responding.
	 */
	<T> void issueTask(Task<T> task, int jobId) throws RemoteException;

	/**
	 * Put the successor to the waiting queue.
	 * 
	 * @param task
	 *            the successor.
	 * @return the ID of the successor.
	 * @throws RemoteException
	 *             occurs if there is a communication problem or the remote
	 *             service is not responding.
	 */
	<T> void suspendTask(Task<T> task, int jobId) throws RemoteException;

	/**
	 * Insert the argument to the corresponding slot in the closure.
	 * 
	 * @param arg
	 *            the input argument.
	 * @param id
	 *            the id of the successor.
	 * @param slotIndex
	 *            the corresponding slot.
	 * @throws RemoteException
	 *             occurs if there is a communication problem or the remote
	 *             service is not responding.
	 */
	<T> void insertArg(Argument<T> arg, long id, int slotIndex, int jobId) throws RemoteException;

	/**
	 * Remove and return the task from the task queue.
	 * 
	 * @return the task to be removed and returned.
	 * @throws RemoteException
	 *             occurs if there is a communication problem or the remote
	 *             service is not responding.
	 * @throws InterruptedException
	 *             occurs when a thread is waiting, sleeping, or otherwise
	 *             occupied, and the thread is interrupted, either before or
	 *             during the activity.
	 */
	<T> Task<T> fetchTask(int jobId) throws RemoteException, InterruptedException;

	/**
	 * Put the final result to the root task into the result
	 * queue.
	 * 
	 * @param result the final result to the root task.
	 * @throws RemoteException
	 *             occurs if there is a communication problem or the remote
	 *             service is not responding.
	 * @throws InterruptedException
	 *             occurs when a thread is waiting, sleeping, or otherwise
	 *             occupied, and the thread is interrupted, either before or
	 *             during the activity.
	 */
	<T> void setupResult(T result, int jobId) throws RemoteException, InterruptedException;

	/**
	 * Put the root task into the task queue.
	 * @param job the root task
	 * @throws RemoteException
	 *             occurs if there is a communication problem or the remote
	 *             service is not responding.
	 * @throws InterruptedException
	 *             occurs when a thread is waiting, sleeping, or otherwise
	 *             occupied, and the thread is interrupted, either before or
	 *             during the activity.
	 */
	<T> void startJob(int jobId) throws RemoteException, InterruptedException;
	
	public long getTaskId(int jobId) throws RemoteException;
	
	public Double getShared(int jobId) throws RemoteException;
	
	public void putShared(Double shared, int jobId) throws RemoteException;
	
	public Space getMirror() throws RemoteException;
	
	public int prepareJob(Job job) throws RemoteException;
	
	public void addMirror(Space space) throws RemoteException;
	
	public void checkPoint(JobContext jobContext, int jobId) throws RemoteException;
	
	public void resumeJob(int jobId) throws RemoteException;

	<T> void clearShadow(Task<T> task, int jobId) throws RemoteException;
}