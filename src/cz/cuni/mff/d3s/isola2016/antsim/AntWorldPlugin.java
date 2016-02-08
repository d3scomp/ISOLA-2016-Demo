package cz.cuni.mff.d3s.isola2016.antsim;

import java.util.Collection;
import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class AntWorldPlugin implements DEECoPlugin {
	public static double SAME_POS_DIST_M = 0.01;
	
	public Position antHill;
	public int collectedFoodPieces;
	public Collection<FoodSource> foodSources;

	@Override
	public List<Class<? extends DEECoPlugin>> getDependencies() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void init(DEECoContainer container) throws PluginInitFailedException {
		// TODO Auto-generated method stub

	}
	
	Collection<FoodSource> getSensedFood(Position position, double range) {
		List<FoodSource> sensed = new LinkedList<>();
		
		for(FoodSource source: foodSources) {
			if(source.position.euclidDistanceTo(position) <= range) {
				sensed.add(source);
			}
		}
		
		return sensed;
	}

}
