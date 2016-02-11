package cz.cuni.mff.d3s.isola2016.demo;

import java.io.Serializable;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.Local;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.timer.CurrentTimeProvider;
import cz.cuni.mff.d3s.isola2016.antsim.AntPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.AntPlugin.State;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;
import cz.cuni.mff.d3s.jdeeco.network.omnet.OMNeTSimulation.Timer;
import cz.cuni.mff.d3s.jdeeco.position.Position;

@Component
public class AntComponent {
	@SuppressWarnings("serial")
	public static class FoodSourceEx extends FoodSource implements Serializable {
		public FoodSourceEx(Position position, Integer portions) {
			super(position, portions);
		}
		
		public FoodSourceEx(FoodSource source, long age) {
			super(source.position, source.portions);
			this.timestamp = age;
		}
		
		public long timestamp;
	}
	
	static enum Mode {
		Searching, Pulling
	}
	
	public static final long MAX_FOOD_AGE_MS = 30000;
	public static final double RANDOM_WALK_DIAMETER = 20;

	public String id;
	public Position position;
	public List<FoodSourceEx> foods;
	public Position assignedFoodPosition;
	
	@Local
	public State state;
	
	@Local
	public Mode mode;
	
	@Local
	public CurrentTimeProvider clock;

	@Local
	public AntPlugin ant;
	
	@Local
	public Random rand;
	
	@Local
	public Position curTarget;
	
	@Local
	public Position antHill;

	/// Initial knowledge
	public AntComponent(int id, Random rand, Timer timer, AntPlugin ant, Position antHill) {
		this.id = String.valueOf(id);
		this.rand = rand;
		this.clock = timer;
		this.ant = ant;
		this.foods = new LinkedList<>();
		this.state = State.Free;
		this.mode = Mode.Searching;
		this.antHill = antHill;
	}

	/// Processes
	@Process
	@PeriodicScheduling(period = 500)
	public static void sensePosition(@In("ant") AntPlugin ant, @Out("position") ParamHolder<Position> position) {
		position.value = ant.getPosition();
	}
	
	@Process
	@PeriodicScheduling(period = 500)
	public static void senseState(@In("ant") AntPlugin ant, @Out("state") ParamHolder<State> state) {
		state.value = ant.getState();
	}

	@Process
	@PeriodicScheduling(period = 500)
	public static void senseFood(@In("ant") AntPlugin ant, @In("clock") CurrentTimeProvider clock,
			@InOut("foods") ParamHolder<List<FoodSourceEx>> foods) {
		// Remove old food
		Set<FoodSourceEx> toRemove = new HashSet<>();
		for(FoodSourceEx source: foods.value) {
			if(clock.getCurrentMilliseconds() - source.timestamp > MAX_FOOD_AGE_MS) {
				toRemove.add(source);
			}
		}
		foods.value.removeAll(toRemove);
		
		// Add new sensed food
		for(FoodSource newSource: ant.getSensedFood()) {
			// Try to update existing one
			boolean updated = false;
			for(FoodSourceEx oldSource: foods.value) {
				if(PosUtils.isSame(oldSource.position, newSource.position)) {
					oldSource.timestamp = clock.getCurrentMilliseconds();
					updated = true;
				}
			}
			
			// Skip adding if update was performed
			if(updated) {
				continue;
			}
			
			// Add new source
			foods.value.add(new FoodSourceEx(newSource, clock.getCurrentMilliseconds()));
		}
	}

	@Process
	@PeriodicScheduling(period = 5000)
	public static void printStatus(@In("clock") CurrentTimeProvider clock, @In("id") String id,
			@In("position") Position position, @In("state") State state, @In("foods") List<FoodSourceEx> foods) {
		if(position == null) {
			return;
		}
		
		System.out.format("%06d: Ant %s, %s, %s, foods: ", clock.getCurrentMilliseconds(), id, position, state);
		
		for(FoodSourceEx source: foods) {
			System.out.format("%f, ", source.position.euclidDistanceTo(position));
		}
		
		System.out.println();
	}

	@Process
	@PeriodicScheduling(period = 1000)
	public static void searchByRandomWalk(@In("ant") AntPlugin ant, @In("mode") Mode mode, @In("rand") Random rand) {
		if(mode != Mode.Searching) {
			return;
		}
		
		if(ant.isAtTarget()) {
			ant.setTarget(PosUtils.getRandomPosition(rand, 0, 0, RANDOM_WALK_DIAMETER));
		}
	}
}
