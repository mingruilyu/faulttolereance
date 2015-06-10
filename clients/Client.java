package clients;

import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;

import api.Job;
import api.Space;
/**
 *
 * @author Mingrui Lyu
 * @param <T>
 * return type the Task that this Client executes.
 */
public class Client<T> extends JFrame {
	protected T taskReturnValue;
	private long clientStartTime;
	protected Space space;
	protected Space mirrorSpace;
	protected int jobId;
	private int compNum;
	private Boolean finalFlag;
	private DisplayThread displayThread;
	public Client(final String title, final String domainName, final int compNum) 
			throws RemoteException, NotBoundException,
			MalformedURLException {
		setTitle(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		String url = "rmi://" + domainName + ":" + Space.PORT + "/"
				+ Space.SERVICE_NAME;
		this.space = (Space) Naming.lookup(url);
		this.compNum = compNum;
		this.finalFlag = new Boolean(false);
	}

	public void begin(DisplayThread displayThread) {
		clientStartTime = System.nanoTime();

		this.displayThread = displayThread;
		displayThread.setFinalFlag(this.finalFlag);
		displayThread.setJFrame(this);
		displayThread.setJobId(jobId);
		displayThread.setSpace(space);
		displayThread.start();
	}

	public void end() {		
		this.displayThread.setFinalFlag(new Boolean(true));
		Logger.getLogger(Client.class.getCanonicalName()).log(Level.INFO,
				"Client time: {0} ms.",
				(System.nanoTime() - clientStartTime) / 1000000);
	}

/*	public void add(final JLabel jLabel) {
		final Container container = getContentPane();
		container.setLayout(new BorderLayout());
		container.add(new JScrollPane(jLabel), BorderLayout.CENTER);
		pack();
		setVisible(true);
	}*/
	
	public T runJob(Job<T> job, DisplayThread displayThread){
		final long taskStartTime = System.nanoTime();
		T value = null;
		try {
			this.mirrorSpace = this.space.getMirror();
			this.jobId = this.space.prepareJob(job,compNum);
			if(this.jobId == -1) {
				System.out.println("Currently No computer available! Try later!");
				return null;
			}
			System.out.println("Space prepare job "+this.jobId);
			this.space.startJob(this.jobId);
			
			this.begin(displayThread);
			value = this.space.takeFinalResult(this.jobId);
			this.end();
			System.out.println("end");
			this.space.synchronizeFinalResult(this.jobId);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			this.space = this.mirrorSpace;
			try {
				Long start = System.currentTimeMillis();
				this.displayThread.setSpace(this.space);
				this.displayThread.start();
				this.space.resumeJob(this.jobId);
				Long end = System.currentTimeMillis();
				System.out.println("Resuming jobs: " + (end-start));
				value = this.space.takeFinalResult(this.jobId);
				this.end();
				this.space.synchronizeFinalResult(this.jobId);
			} catch (RemoteException | InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		final long taskRunTime = (System.nanoTime() - taskStartTime) / 1000000;
		System.out.println("Job runtime = " + taskRunTime);
		return value;
	}
}