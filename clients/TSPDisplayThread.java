package clients;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Image;
import java.awt.image.BufferedImage;
import java.rmi.RemoteException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;

import api.Shared;

public class TSPDisplayThread extends DisplayThread{
	@Override
	public void run() {
		super.run();

		Shared result = null;
		do {
			try {
				result = space.takeIntermediateResult(this.jobId);
				System.out.println("Take intermediate result!");
				//System.out.println(result.cities);
			} catch (RemoteException | InterruptedException e) {
				System.out.println("Display thread return!");
				try {
					System.out.println("Before wait");
					synchronized (this) {
						wait();
					}
					System.out.println("After wait");
				} catch (InterruptedException e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				//e.printStackTrace();
			}
			if(container.getComponentCount() != 0)
				container.remove(0);
			container.add(new JScrollPane(this.getLabel(result.cities.toArray(new Integer[0]))), 
										  BorderLayout.CENTER);
			this.jFrame.pack();
			this.jFrame.setVisible(true);
		} while (!this.finalFlag);
		System.out.println("out of display loop");
	}
	
	public JLabel getLabel(final Integer[] tour) {
		Logger.getLogger(ClientEuclideanTsp.class.getCanonicalName()).log(
				Level.INFO, tourToString(tour));

		// display the graph graphically, as it were
		// get minX, maxX, minY, maxY, assuming they 0.0 <= mins
		double minX = ClientEuclideanTsp.CITIES[0][0], maxX = ClientEuclideanTsp.CITIES[0][0];
		double minY = ClientEuclideanTsp.CITIES[0][1], maxY = ClientEuclideanTsp.CITIES[0][1];
		for (double[] cities : ClientEuclideanTsp.CITIES) {
			if (cities[0] < minX)
				minX = cities[0];
			if (cities[0] > maxX)
				maxX = cities[0];
			if (cities[1] < minY)
				minY = cities[1];
			if (cities[1] > maxY)
				maxY = cities[1];
		}

		// scale points to fit in unit square
		final double side = Math.max(maxX - minX, maxY - minY);
		double[][] scaledCities = new double[ClientEuclideanTsp.CITIES.length][2];
		for (int i = 0; i < ClientEuclideanTsp.CITIES.length; i++) {
			scaledCities[i][0] = (ClientEuclideanTsp.CITIES[i][0] - minX) / side;
			scaledCities[i][1] = (ClientEuclideanTsp.CITIES[i][1] - minY) / side;
		}

		final Image image = new BufferedImage(NUM_PIXALS, NUM_PIXALS,
				BufferedImage.TYPE_INT_ARGB);
		final Graphics graphics = image.getGraphics();

		final int margin = 10;
		final int field = NUM_PIXALS - 2 * margin;
		// draw edges
		graphics.setColor(Color.BLUE);
		int x1, y1, x2, y2;
		System.out.println(tour.length);
		for(int i=0;i<tour.length;i++) {
			System.out.println(tour[i]);
		}

		int city1 = tour[0], city2;
		x1 = margin + (int) (scaledCities[city1][0] * field);
		y1 = margin + (int) (scaledCities[city1][1] * field);
		for (int i = 1; i < ClientEuclideanTsp.CITIES.length; i++) {
			city2 = tour[i];
			x2 = margin + (int) (scaledCities[city2][0] * field);
			y2 = margin + (int) (scaledCities[city2][1] * field);
			graphics.drawLine(x1, y1, x2, y2);
			x1 = x2;
			y1 = y2;
		}
		city2 = tour[0];
		x2 = margin + (int) (scaledCities[city2][0] * field);
		y2 = margin + (int) (scaledCities[city2][1] * field);
		graphics.drawLine(x1, y1, x2, y2);

		// draw vertices
		final int VERTEX_DIAMETER = 6;
		graphics.setColor(Color.RED);
		for (int i = 0; i < ClientEuclideanTsp.CITIES.length; i++) {
			int x = margin + (int) (scaledCities[i][0] * field);
			int y = margin + (int) (scaledCities[i][1] * field);
			graphics.fillOval(x - VERTEX_DIAMETER / 2, y - VERTEX_DIAMETER / 2,
					VERTEX_DIAMETER, VERTEX_DIAMETER);
		}
		final ImageIcon imageIcon = new ImageIcon(image);
		return new JLabel(imageIcon);
	}
	
	private String tourToString(Integer[] cities) {
		StringBuilder stringBuilder = new StringBuilder();
		stringBuilder.append("Tour: ");
		for (Integer city : cities) {
			stringBuilder.append(city).append(' ');
		}
		return stringBuilder.toString();
	}
}
