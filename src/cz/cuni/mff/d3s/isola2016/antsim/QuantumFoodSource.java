package cz.cuni.mff.d3s.isola2016.antsim;

import java.util.LinkedHashSet;
import java.util.Set;

import cz.cuni.mff.d3s.isola2016.demo.FoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

@SuppressWarnings("serial")
public class QuantumFoodSource extends FoodSource {
	private static int counter = 0;

	public final Integer quantumId;
	QuantumFoodSource other;

	private QuantumFoodSource(Position position, Integer portions, Integer quantumId, long timestamp) {
		super(position, portions, timestamp);
		this.quantumId = quantumId;
	}
	
	public Set<Position> getQuantumPositions() {
		Set<Position> ret = new LinkedHashSet<>(2);
		ret.add(position);
		ret.add(other.position);
		return ret;
	}

	public static Set<QuantumFoodSource> createQuantumFoodPair(Position posA, Position posB, Integer portions) {
		Set<QuantumFoodSource> ret = new LinkedHashSet<>(2);
		int quantumId = counter++;
		QuantumFoodSource a = new QuantumFoodSource(posA, portions, quantumId, 0);
		QuantumFoodSource b = new QuantumFoodSource(posB, portions, quantumId, 0);
		a.other = b;
		b.other = a;
		ret.add(a);
		ret.add(b);
		return ret;
	}
	
	@Override
	public FoodSource cloneWithTimestamp(long timestamp) {
		QuantumFoodSource ret = new QuantumFoodSource(position, portions, quantumId, timestamp);
		ret.other = other;
		return ret;
	}
}
