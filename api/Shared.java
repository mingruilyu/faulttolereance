package api;

import java.util.List;

public class Shared {
	public final Double shortestDistance;
	public final List<Integer> cities;
	
	public Shared(Double shortestDouble, List<Integer> cities) {
		this.shortestDistance = shortestDouble;
		this.cities = cities;
	}
}
