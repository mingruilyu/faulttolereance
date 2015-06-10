package clients;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.net.MalformedURLException;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import api.Shared;
import jobs.JobEuclideanTsp;
import tasks.TaskEuclideanTsp;

public class ClientEuclideanTsp extends Client<List<Integer>> {
	
	public static final double[][] CITIES = {
		{ 1, 1 },
		{ 8, 1 },
		{ 8, 8 },
		{ 1, 8 },
		{ 2, 2 },
		{ 7, 2 },
		{ 7, 7 },
		{ 2, 7 },
		{ 3, 3 },
		{ 6, 3 },
		{ 6, 6 },
		{ 3, 6 },
		{ 4, 4 },
		//{ 5, 4 }
		//{ 5, 5 },
		//{ 4, 5 }
	};


	public ClientEuclideanTsp(String serverDomain,String compNum) throws RemoteException,
			NotBoundException, MalformedURLException {
		super("Euclidean TSP", serverDomain, Integer.parseInt(compNum));
	}

	public static void main(String[] args) throws Exception {
		System.setSecurityManager(new SecurityManager());
		final ClientEuclideanTsp client = new ClientEuclideanTsp(args[0], args[1]);
		
		final List<Integer> value = client.runJob(new JobEuclideanTsp(13), 
				new TSPDisplayThread());
//		if(value == null) return;
//		System.out.println(value);
	}


}
