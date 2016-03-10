package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Collection;

import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class ProactiveSolver implements AntAssignmetSolver {
	@Override
	public Position solve(Collection<AntInfo> ants, Collection<FoodSource> foods, AntInfo localAnt, Position antHill) {
		Position nearestPosition = null;
		
		for(FoodSource source: foods) {
			if(nearestPosition == null || source.position.euclidDistanceTo(localAnt.position) < nearestPosition.euclidDistanceTo(localAnt.position)) {
				nearestPosition = source.position;
			}
		}
		
		return nearestPosition;
	}
}
