package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Collection;

import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public interface AntAssignmetSolver {
	Position solve(Collection<AntInfo> ants, Collection<FoodSource> foods, AntInfo localAnt);
}
