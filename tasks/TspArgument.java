package tasks;

import java.util.List;

import api.Argument;

public class TspArgument extends Argument<List<Integer>>{
	public static final long serialVersionUID = 227L;
	
	double minCost;
	public TspArgument(List<Integer> result, double minCost, long time) {
		super(result, time);
	}
	

	public double getMinCost() {
		return minCost;
	}

	public void setMinCost(double minCost) {
		this.minCost = minCost;
	}
}
