package api;

import java.io.Serializable;
import java.util.List;

public class Shared implements Serializable{
	public final Double shortestDistance;
	public final List<Integer> cities;
	public static final long serialVersionUID = 227L;
	
	public Shared(Double shortestDouble, List<Integer> cities) {
		this.shortestDistance = shortestDouble;
		this.cities = cities;
	}
}
