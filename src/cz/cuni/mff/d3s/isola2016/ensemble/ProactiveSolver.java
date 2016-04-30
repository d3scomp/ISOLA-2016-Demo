package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Collection;

import cz.cuni.mff.d3s.isola2016.antsim.AbstractAntWorldPlugin;
import cz.cuni.mff.d3s.isola2016.demo.BigAnt;
import cz.cuni.mff.d3s.isola2016.demo.FoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class ProactiveSolver extends AntAssignmetSolver {
	public ProactiveSolver(AbstractAntWorldPlugin world) {
		super(world);
	}

	@Override
	public void solve(Collection<BigAnt> ants, Collection<FoodSource> foods, Position antHill, long curTime) {
		for (BigAnt ant : ants) {
			FoodSource nearestSource = null;
			for (FoodSource source : foods) {
				if (nearestSource == null || source.position.euclidDistanceTo(ant.position) < nearestSource.position
						.euclidDistanceTo(ant.position)) {
					nearestSource = source;
				}
			}
			ant.assignedFood = nearestSource.position;
			ant.assignedFoodTime = curTime;
			ant.accumulatedFitness += 1 / nearestSource.position.euclidDistanceTo(ant.position);
		}
	}
}
