package clients;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import api.Shared;
import api.Space;

public class DisplayThread implements Runnable {
	public static final int NUM_PIXALS = 600;
	protected Container container;
	protected int jobId;
	protected JFrame jFrame;
	protected Space space;
	protected Boolean finalFlag;
	
	public void setJobId(int jobId) {
		this.jobId = jobId;
	}
	
	public void setFinalFlag(Boolean finalFlag) {
		this.finalFlag = finalFlag;
	}
	
	public void setSpace(Space space) {
		this.space = space;
	}
	
	public void setJFrame(JFrame jframe) {
		this.jFrame = jframe;
		this.container = jFrame.getContentPane();
		this.container.setLayout(new BorderLayout());
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		
	}
	
}