package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Collection;

import cz.cuni.mff.d3s.isola2016.antsim.AbstractAntWorldPlugin;
import cz.cuni.mff.d3s.isola2016.demo.BigAnt;
import cz.cuni.mff.d3s.isola2016.demo.FoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public abstract class AntAssignmetSolver {
	protected AbstractAntWorldPlugin world;
	
	public AntAssignmetSolver(AbstractAntWorldPlugin world) {
		this.world = world;
	}
	
	public abstract void solve(Collection<BigAnt> ants, Collection<FoodSource> foods, Position antHill, long curTime);
}
