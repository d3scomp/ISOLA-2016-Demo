package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.d3s.isola2016.antsim.QuantumFoodSource;
import cz.cuni.mff.d3s.isola2016.demo.BigAnt;
import cz.cuni.mff.d3s.isola2016.demo.FoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class QuantumHeuristicSolver {
	private static class QuantumFoodPair {
		public QuantumFoodSource a;
		public QuantumFoodSource b;

		void add(QuantumFoodSource src) {
			if(a == null) {
				a = src;
			} else {
				if(b == null) {
					if(!a.quantumId.equals(src.quantumId)) {
						throw new UnsupportedOperationException("Adding quantum source to pair, but the id is not matching: " + a.quantumId + " != " + src.quantumId);
					}
					b = src;
				} else {
					throw new UnsupportedOperationException("Adding third source to quantum pair"); 
				}
			}
		}
	}
	
	private Map<Integer, QuantumFoodPair> buildQIdToSourceMap(Collection<FoodSource> foods) {
		// Build a map from quantum ids to foods
		Map<Integer, QuantumFoodPair> qIdToFoods = new HashMap<>();
		for(FoodSource s: foods) {
			QuantumFoodSource qfood = (QuantumFoodSource) s;
			if(!qIdToFoods.containsKey(qfood.quantumId)) {
				qIdToFoods.put(qfood.quantumId, new QuantumFoodPair());
			}
			qIdToFoods.get(qfood.quantumId).add(qfood);
		}
		
		return qIdToFoods;
	}
	
	private static class AntAntQFood {
		public final BigAnt antA;
		public final BigAnt antB;
		public final QuantumFoodSource srcA;
		public final QuantumFoodSource srcB;
		public final int quantumId;
		public final double price;
		
		public AntAntQFood(BigAnt antA, BigAnt antB, QuantumFoodSource srcA, QuantumFoodSource srcB) {
			this.antA = antA;
			this.antB = antB;
			
			quantumId = srcA.quantumId;
			if(!srcB.quantumId.equals(quantumId)) {
				throw new UnsupportedOperationException("Quantum food pair ids do not match: " + quantumId + " != " + srcB.quantumId);
			}
			
			double straightPrice = antA.position.euclidDistanceTo(srcA.position) + antB.position.euclidDistanceTo(srcB.position);
			double crossedPrice = antA.position.euclidDistanceTo(srcB.position) + antB.position.euclidDistanceTo(srcA.position);
			
			if(straightPrice <= crossedPrice) {
				this.srcA = srcA;
				this.srcB = srcB;
				this.price = straightPrice;
			} else {
				this.srcA = srcB;
				this.srcB = srcA;
				this.price = crossedPrice;
			}
		}
	}

	public void solve(Collection<BigAnt> ants, Collection<FoodSource> foods, BigAnt localAnt, Position antHill) {
		LinkedHashSet<BigAnt> remaingAnts = new LinkedHashSet<>(ants);
		
		Map<Integer, QuantumFoodPair> qIdToFoods = buildQIdToSourceMap(foods);
		
		while(!(remaingAnts.isEmpty() || qIdToFoods.keySet().isEmpty())) {
			Set<AntAntQFood> options = new LinkedHashSet<>();
			
			// Generate all ant pairs
			for(BigAnt a: remaingAnts) {
				for(BigAnt b: remaingAnts) {
					if(a.equals(b)) {
						continue;
					}
					
					for(QuantumFoodPair srcs: qIdToFoods.values()) {
						if(srcs.a != null && srcs.b != null) {
							options.add(new AntAntQFood(a, b, srcs.a, srcs.b));
						}
					}
				}
			}
			
			// Get the best option
			AntAntQFood best = null;
			for(AntAntQFood option: options) {
				if(best == null || best.price > option.price) {
					best = option;
				}
			}
			
			if(best == null) {
				break;
			}
			
			// We should remember all best options, but we are interested only in the local one
			if(best.antA == localAnt) {
	//			return new Result(best.srcA, best.antB); 
			}
			if(best.antB == localAnt) {
	//			return new Result(best.srcB, best.antA); 
			}
			
			// Remove best option from remaining
			remaingAnts.remove(best.antA);
			remaingAnts.remove(best.antB);
			qIdToFoods.remove(best.quantumId);
		}
		
	//	return new Result(null, null);
	}
}
