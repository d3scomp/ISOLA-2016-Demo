package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Collection;

import cz.cuni.mff.d3s.isola2016.demo.FoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class ProactiveSolver implements AntAssignmetSolver {
	@Override
	public Result solve(Collection<AntInfo> ants, Collection<FoodSource> foods, AntInfo localAnt, Position antHill) {
		FoodSource nearestSource = null;

		for (FoodSource source : foods) {
			if (nearestSource == null || source.position.euclidDistanceTo(localAnt.position) < nearestSource.position
					.euclidDistanceTo(localAnt.position)) {
				nearestSource = source;
			}
		}

		return new Result(nearestSource, null);
	}
}
