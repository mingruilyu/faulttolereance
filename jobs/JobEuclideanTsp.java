package jobs;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

import tasks.TaskEuclideanTsp;
import api.Job;
import api.Space;
import api.Task;

public class JobEuclideanTsp implements Job<List<Integer>>, Serializable{
	public static final long serialVersionUID = 227L;
	private int n;
	public JobEuclideanTsp(int n) {
		this.n = n;
	}
	@Override
	public Task<List<Integer>> toTask(Space space) {
		List<Integer> prevCities = new ArrayList<Integer> ();
		double restDistance = 0;
		// restDistance includes the sum of two least edges for 
		// all the city except the city 0
		for(int i = 1; i < TaskEuclideanTsp.LEAST_COST_EDGES.length; i ++) {
			restDistance += TaskEuclideanTsp.LEAST_COST_EDGES[i][0] 
							+ TaskEuclideanTsp.LEAST_COST_EDGES[i][1]; 
		}
		// initial partialDistance is the least edge of city 0
		return new TaskEuclideanTsp(0, Task.NO_PARENT, Task.NO_PARENT, n-1, 
									prevCities, n, restDistance,  
									TaskEuclideanTsp.LEAST_COST_EDGES[0][0]);
	}
	@Override
	public String toString() {
		
		return "JobEuclideanTsp: " + this.n;
	}	
}
