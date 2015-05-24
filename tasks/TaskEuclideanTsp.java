package tasks;

import java.io.Serializable;
import java.rmi.RemoteException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import api.Space;
import api.Task;

/**
 * TaskEuclideanTsp extends from the Task abstract class. This task solves a
 * Traveling Salesman Problem (TSP).
 * 
 * @author Gongxl
 *
 */
public class TaskEuclideanTsp extends Task<List<Integer>> implements
		Serializable {
	public static final long serialVersionUID = 227L;
	private long decomposeTime = 0;
	private int settledCity;
	private int n;
	private List<Integer> settledCities;
	private int total;
	private double restDistance;
	private double partialDistance;
	private double lowerbound;
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
		{ 5, 4 },
		{ 5, 5 },
		{ 4, 5 }
	};
	static final public double[][] DISTANCES = initializeDistances();
	static final public double[][] LEAST_COST_EDGES = initializeEdges();
	/**
	 * construct a TSP task,
	 * 
	 * @param cities
	 *            coordinates of cities
	 * @param settledCity
	 *            the one to settled in this new task
	 * @param space
	 *            to get the space
	 * @param parentId
	 *            inherited from parent
	 * @param slotIndex
	 *            set where should the result go
	 * @param n
	 *            how many cities remain unsettled
	 * @param prevCities
	 *            the cities have been settled
	 * @param total
	 *            the total of cities
	 */
	public TaskEuclideanTsp(int settledCity, long parentId, int slotIndex, 
							int n, List<Integer> prevCities, int total,
							double restDistance, double partialDistance) {
		super(parentId, slotIndex);
		this.settledCity = settledCity;
		this.n = n;
		this.total = total;
		this.settledCities = new ArrayList<Integer>(prevCities);
		// ========================================================
		//update the lowerbound in O(1)
		if(this.settledCities.size() == 0) {
			this.partialDistance = partialDistance + LEAST_COST_EDGES[0][1];
			this.restDistance = restDistance;
		} else {
			int lastCity = this.settledCities.get(this.settledCities.size() - 1);
			this.restDistance = restDistance - LEAST_COST_EDGES[settledCity][0] 
								- LEAST_COST_EDGES[settledCity][1];
			this.partialDistance = partialDistance - LEAST_COST_EDGES[lastCity][0] 
							   	   + DISTANCES[lastCity][settledCity] 
							   	   + LEAST_COST_EDGES[settledCity][0];
			this.lowerbound = this.restDistance / 2 + this.partialDistance;
		}
		// ========================================================
		settledCities.add(settledCity);
		if (n > 7) {
			this.missingArgCount = n - 1;
			for (int i = 0; i < n - 1; i++) {
				this.argList.add(null);
			}
		} else this.missingArgCount = -1;
	}

	/**
	 * convert the List to Set
	 * 
	 * @param a
	 *            list of cities
	 */
	private void list2Set(List<Integer> list, Set<Integer> set) {
		for (int i : list) {
			set.add(i);
		}
	}

	/**
	 * get the minimum cost of a list of cities first merge the list of
	 * settledCities with each permutation and find the minimum cost one
	 * 
	 * @param settledCities
	 *            the list of settled cities
	 * @param permutation
	 *            the list of permutations
	 * @return the minimum cost
	 */
	private List<Integer> getMinCost(List<Integer> settledCities,
			List<ArrayList<Integer>> permutation) {
		Double minCost = Double.MAX_VALUE;
		List<Integer> minList = new ArrayList<Integer>();
		for (List<Integer> list : permutation) {
			List<Integer> temp = new ArrayList<Integer>();
			temp.addAll(settledCities);
			temp.addAll(list);
			double cost = calculateCost(temp);
			if (cost < minCost) {
				minCost = cost;
				minList.clear();
				minList.addAll(temp);
			}
		}
		return minList;
	}

	/**
	 * get the cost of travel
	 * 
	 * @param trail
	 *            a list of cities
	 * @return the cost of travel along these city in this order
	 */
	
	static private double[][] initializeDistances() {
		double[][] distances = new double[CITIES.length][CITIES.length];
		for (int i = 0; i < CITIES.length; i++)
			for (int j = 0; j < i; j++) {
				distances[i][j] = distances[j][i] = distance(CITIES[i],
						CITIES[j]);
			}
		return distances;
	}

	static private double[][] initializeEdges() {
		double minEdge_1, minEdge_2;
		double[][] leastCostEdges = new double[CITIES.length][2];
		for(int i = 0; i < CITIES.length; i ++) {
			minEdge_1 = Integer.MAX_VALUE;
			minEdge_2 = Integer.MAX_VALUE;
			for(int j = 0; j < CITIES.length; j ++) {
				if(i == j) continue;
				if(DISTANCES[i][j] < minEdge_1) {
					minEdge_1 = DISTANCES[i][j];
					minEdge_2 = minEdge_1;
				} else if(DISTANCES[i][j] < minEdge_2) {
					minEdge_2 = DISTANCES[i][j];
				}
			}
			leastCostEdges[i][0] = minEdge_1;
			leastCostEdges[i][1] = minEdge_2;
		}
		return leastCostEdges;
	}

	private static double distance(final double[] city1, final double[] city2) {
		final double deltaX = city1[0] - city2[0];
		final double deltaY = city1[1] - city2[1];
		return Math.sqrt(deltaX * deltaX + deltaY * deltaY);
	}
	
	private double calculateCost(List<Integer> trail) {
		double totalCost = 0;
		int lastCity = 0, curCity;
		for (int i = 0; i < trail.size(); i++) {
			curCity = trail.get(i);
			totalCost += DISTANCES[curCity][lastCity];
			lastCity = curCity;
		}
		totalCost += DISTANCES[0][lastCity];
		return totalCost;
	}
	/**
	 * generate permutations from 0 to the total number of cities, do not
	 * include these settled cities
	 * 
	 * @param set
	 *            a set of cities which have already been settled
	 * @return a list of city list
	 */
	private List<ArrayList<Integer>> generatePermutation(Set<Integer> set) {
		ArrayList<ArrayList<Integer>> base = new ArrayList<ArrayList<Integer>>();
		base.add(new ArrayList<Integer>());

		for (int i = 1; i < total; i++) {
			if (set.contains(i))
				continue;
			ArrayList<ArrayList<Integer>> newBase = new ArrayList<ArrayList<Integer>>();
			for (ArrayList<Integer> list : base) {
				for (int j = 0; j <= list.size(); j++) {
					ArrayList<Integer> temp = new ArrayList<Integer>();
					temp.addAll(list);
					temp.add(j, i);
					newBase.add(temp);
				}
			}
			base = newBase;
		}
		return base;
	}

	/**
	 * output the taskNO
	 */
	public String toString() {
		return "TSP taskNO " + this.settledCity;
	}

	/**
	 * collect all the argument and get the final result
	 * 
	 * @return the list with the minimum cost of travel
	 * @throws RemoteException 
	 */
	private TspArgument getResult() throws RemoteException {
		long start = System.nanoTime();
		List<Integer> minList = null;
		double minCost = Double.MAX_VALUE;
		long maxTime = 0;
		for (int i = 0; i < this.getArgCount(); i++) {
			List<Integer> tempList = this.getArg(i).getArg();
			if(tempList == null) continue;
			double tempCost = calculateCost(tempList);
			if (tempCost < minCost) {
				minCost = tempCost;
				minList = tempList;
			}
			long tempTime = this.getArg(i).getTime();
            if(tempTime > maxTime) maxTime = tempTime;
		}
		long end = System.nanoTime();
		synchronized(t1) {
			t1 += end - start;
		}
		return new TspArgument(minList, minCost, end - start + maxTime + this.decomposeTime);
	}

	/**
	 * the run method check the number if missing argument, if it is -1, then
	 * the result of this task have been done during construction, get the
	 * result and feedback it. if it is 0, means that all the argument have been
	 * selected, then just get the result by calling the get result method, and
	 * feedback the result. if it is larger than 0, need to settle one city each
	 * time when constructing new task
	 */
	@Override
	public void run(Space space) throws RemoteException {
		double upperbound = space.getShared();
		TspArgument tspArgument;
		long time = 0;

		if (this.missingArgCount <= 0) {
			List<Integer> result = null;
			if (this.missingArgCount == -1) {
				// direct calculation
				long start = System.nanoTime();
				double minCost = 0;
				if(this.lowerbound < upperbound) {
					Set<Integer> set = new HashSet<Integer>();
					list2Set(settledCities, set);
					List<ArrayList<Integer>> permutation = generatePermutation(set);
					result = getMinCost(settledCities, permutation);
					minCost = calculateCost(result);
				} else {
					System.out.println("Pruned!");
					result = null;
					minCost = Double.MAX_VALUE;
				}
				long end = System.nanoTime();
				time = end - start;
				synchronized(t1) {
					t1 += time;
				}
				tspArgument = new TspArgument(result, minCost, time);
			} else {
				// calculate the minimum route of argument list
				// notice that the result here can also be null
				tspArgument = getResult();
			}

			if(result != null) {
				double minCost = calculateCost(result);
				if(minCost < upperbound) 
					space.putShared(minCost);
			}
			this.feedback(tspArgument, space);
		} else {
			// We must make sure that the suspend come before spawn,
			// otherwise we will have children that cannot find father
			// because we increment the parentId before we suspend the 
			// task. If we do this in a reverse order, It could happen that 
			// the children has set its parent Id to some value yet the
			// successor that correspond to this id was not put into waiting
			// queue. 
			
			long start = System.nanoTime();
			long parentId = space.getTaskId();
			space.suspendTask(this, parentId);
			this.spawn(space, parentId);
			long end = System.nanoTime();
			synchronized(t1) {
				t1 += end - start;
			}
			this.decomposeTime += (end - start);
//			System.out.println("Decompose time: " + decomposeTime);
		}
	}

	@Override
	public void spawn(Space space, long parentId)
			throws RemoteException {	
		Set<Integer> set = new HashSet<Integer>();
		list2Set(settledCities, set);
		int settledCity = 0;
		for (int i = 0; i < n - 1; i++) {
			while (set.contains(settledCity)) {
				settledCity++;
			}
			space.issueTask(new TaskEuclideanTsp(settledCity, parentId, i, n - 1, settledCities, 
												 total, this.restDistance, this.partialDistance));
			settledCity++;
		}
	}
}
