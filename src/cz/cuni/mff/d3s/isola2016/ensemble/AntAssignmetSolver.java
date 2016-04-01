package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Collection;

import cz.cuni.mff.d3s.isola2016.demo.FoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public interface AntAssignmetSolver {
	public class Result {
		public FoodSource food;
		public AntInfo assistingAnt;
		
		public Result(FoodSource food, AntInfo assitant) {
			this.food = food;
			this.assistingAnt = assitant;
		}
	}
	
	Result solve(Collection<AntInfo> ants, Collection<FoodSource> foods, AntInfo localAnt, Position antHill);
}
