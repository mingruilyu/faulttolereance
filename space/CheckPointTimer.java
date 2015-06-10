package space;

import java.rmi.RemoteException;
import java.util.Timer;
import java.util.TimerTask;

import api.JobContext;
import api.Space;

public class CheckPointTimer extends Timer{
	private CheckPointTask checkpointTask;
	
	public CheckPointTimer(JobContext jobContext, Space mirror, int jobId) {
		this.checkpointTask = new CheckPointTask(jobContext, mirror, jobId);
		this.scheduleAtFixedRate(this.checkpointTask, 0, 5000);
	}
	
	private class CheckPointTask extends TimerTask {
		private JobContext jobContext;
		private Space mirror;
		private int jobId;
		public CheckPointTask(JobContext jobContext, Space mirror, int jobId) {
			this.jobContext = jobContext;
			this.mirror = mirror;
			this.jobId = jobId;
		}
		
		@Override
		public void run() {
			System.out.println("Checkpoint running!");
			synchronized(this.jobContext.shadow) {
				synchronized(this.jobContext.waitingQueue) {
					synchronized(this.jobContext.readyQueue) {
						try {
							this.mirror.checkPoint(this.jobContext, jobId);
							System.out.println("Checkpoint!");
						} catch (RemoteException e) {
							e.printStackTrace();
						}
					}
				}
			}
		}
	}
}


