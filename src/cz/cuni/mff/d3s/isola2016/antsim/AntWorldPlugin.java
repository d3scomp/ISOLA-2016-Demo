package cz.cuni.mff.d3s.isola2016.antsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;
import cz.cuni.mff.d3s.deeco.scheduler.Scheduler;
import cz.cuni.mff.d3s.deeco.task.TimerTask;
import cz.cuni.mff.d3s.deeco.task.TimerTaskListener;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class AntWorldPlugin implements DEECoPlugin, TimerTaskListener {
	public static double SAME_POS_DIST_M = 0.01;
	public static long SIM_STEP_MS = 100;

	public Position antHill = new Position(0, 0);
	public int collectedFoodPieces = 0;
	public Collection<AntPlugin> ants = new HashSet<>();
	public Collection<FoodSource> foodSources = new HashSet<>();
	public Collection<FoodPiece> foodPieces = new HashSet<>();
	
	private boolean initialized = false;

	@Override
	public List<Class<? extends DEECoPlugin>> getDependencies() {
		return new ArrayList<>();
	}

	@Override
	public void init(DEECoContainer container) throws PluginInitFailedException {
		if(!initialized) {
			initialized = true;
			Scheduler scheduler = container.getRuntimeFramework().getScheduler();
			new TimerTask(scheduler, this, "AntWorldSimStep", 0, SIM_STEP_MS).schedule();
		}
	}
	
	void registerAnt(AntPlugin ant) {
		ants.add(ant);
	}

	Collection<FoodSource> getSensedFood(Position position, double range) {
		List<FoodSource> sensed = new LinkedList<>();

		for (FoodSource source : foodSources) {
			if (source.position.euclidDistanceTo(position) <= range) {
				sensed.add(source);
			}
		}

		return sensed;
	}

	/**
	 * Gets food source at position
	 * 
	 * @param position
	 *            Position of possible food source
	 * @return FoodSource or null if no FoodSource at position
	 */
	FoodSource getFoodSourceAt(Position position) {
		for (FoodSource source : foodSources) {
			if (source.position.euclidDistanceTo(position) <= SAME_POS_DIST_M) {
				return source;
			}
		}
		return null;
	}

	// Ant world simulation step
	@Override
	public void at(long time, Object triger) {
	//	System.out.println("Ant world simulation step: " + time);
		
		// TODO resolve Locked -> Pulling
		
		// Update/move ants
		for(AntPlugin ant: ants) {
			ant.update();
		}
	}
}
