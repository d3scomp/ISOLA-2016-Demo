package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Map;

import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class HeuristicSolver implements AntAssignmetSolver {
	static class AntAntSource {
		public AntInfo a;
		public AntInfo b;
		public FoodSource nearestFoodSource;
		public double nearestFoodSourcePrice = Double.POSITIVE_INFINITY;
		
		private double getAntAntSourceDistance(AntInfo a, AntInfo b, FoodSource s, Position antHill) {
			double distance = s.position.euclidDistanceTo(antHill);
			distance += a.position.euclidDistanceTo(s.position);
			distance += b.position.euclidDistanceTo(s.position);
			return distance;
		}

		public AntAntSource(AntInfo a, AntInfo b, Collection<FoodSource> foods, Position antHill) {
			this.a = a;
			this.b = b;

			for (FoodSource source : foods) {
				double price = getAntAntSourceDistance(a, b, source, antHill);
				if (nearestFoodSourcePrice > price) {
					nearestFoodSourcePrice = price;
					nearestFoodSource = source;
				}
			}
		}
	}

	static class AntFood {
		public AntInfo ant;
		public FoodSource assignedFoodSource;

		public AntFood(AntInfo ant, FoodSource source) {
			this.ant = ant;
			this.assignedFoodSource = source;
		}
	}

	@Override
	public Position solve(Collection<AntInfo> ants, Collection<FoodSource> foods, AntInfo localAnt, Position antHill) {
		LinkedHashSet<AntInfo> remaing = new LinkedHashSet<>(ants);
		Map<AntInfo, FoodSource> done = new HashMap<>();
		
		while (!remaing.isEmpty()) {
			// Assign nearest foods to pairs
			LinkedHashSet<AntAntSource> antDistances = new LinkedHashSet<>();
			for (AntInfo antA : remaing) {
				for (AntInfo antB : remaing) {
					if(antA != antB) {
						antDistances.add(new AntAntSource(antA, antB, foods, antHill));
					}
				}
			}
			
			// Get optimal pair ant add it to result
			double optimalPairDistance = Double.POSITIVE_INFINITY;
			AntAntSource optimalPair = null;
			
			for(AntAntSource aas: antDistances) {
				if(aas.nearestFoodSourcePrice < optimalPairDistance) {
					optimalPairDistance = aas.nearestFoodSourcePrice;
					optimalPair = aas;
				}
			}
			
			if(optimalPair != null) {
				System.out.println("Optimal pair: " + optimalPair.a.id + " " + optimalPair.b.id + " " + optimalPair.nearestFoodSource.position + " " + optimalPair.nearestFoodSource.portions);
				done.put(optimalPair.a, optimalPair.nearestFoodSource);
				done.put(optimalPair.b, optimalPair.nearestFoodSource);
			
				remaing.remove(optimalPair.a);
				remaing.remove(optimalPair.b);
				foods.remove(optimalPair.nearestFoodSource);
			} else {
				remaing.clear();
				break;
			}
		}
				
		FoodSource assignedFood = done.get(localAnt);
		if(assignedFood == null) {
			return null;
		} else {
			return assignedFood.position;
		}
	}

}
