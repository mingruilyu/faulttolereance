package clients;

import java.awt.BorderLayout;
import java.awt.Container;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import api.Job;
import api.Space;
import api.Task;
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
	private Job<T> job;
	private int jobId;
	public Client(final String title, final String domainName, final Job<T> job) 
			throws RemoteException, NotBoundException,
			MalformedURLException {
		setTitle(title);
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);

		String url = "rmi://" + domainName + ":" + Space.PORT + "/"
				+ Space.SERVICE_NAME;
		this.space = (Space) Naming.lookup(url);
		this.job = job;
	}

	public void begin() {
		clientStartTime = System.nanoTime();
	}

	public void end() {
		Logger.getLogger(Client.class.getCanonicalName()).log(Level.INFO,
				"Client time: {0} ms.",
				(System.nanoTime() - clientStartTime) / 1000000);
	}

	public void add(final JLabel jLabel) {
		final Container container = getContentPane();
		container.setLayout(new BorderLayout());
		container.add(new JScrollPane(jLabel), BorderLayout.CENTER);
		pack();
		setVisible(true);
	}

	public T runTask(){
		final long taskStartTime = System.nanoTime();
		T value = null;
		try {
			this.mirrorSpace = this.space.getMirror();
			this.jobId = this.space.prepareJob(this.job);
			this.space.startJob(this.jobId);
			value = this.space.take(this.jobId);
		} catch (InterruptedException e) {
			e.printStackTrace();
		} catch (RemoteException e) {
			this.space = this.mirrorSpace;
			try {
				this.space.resumeJob(this.jobId);
				value = this.space.take(this.jobId);
			} catch (RemoteException | InterruptedException e1) {
				e1.printStackTrace();
			}
		}

		final long taskRunTime = (System.nanoTime() - taskStartTime) / 1000000;
		Logger.getLogger(Client.class.getCanonicalName()).log(Level.INFO,
				"Task {0}Task time: {1} ms.",
				new Object[] {this.job, taskRunTime });
		return value;
	}
}