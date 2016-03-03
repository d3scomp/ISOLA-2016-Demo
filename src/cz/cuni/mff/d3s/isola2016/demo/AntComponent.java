package cz.cuni.mff.d3s.isola2016.demo;

import java.util.LinkedHashSet;
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
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogger;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.timer.CurrentTimeProvider;
import cz.cuni.mff.d3s.isola2016.antsim.AntPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.AntPlugin.State;
import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.jdeeco.position.Position;

@Component
public class AntComponent {
	public static final long MAX_FOOD_AGE_MS = 300000;
	public static final double RANDOM_WALK_DIAMETER = 15;
	public static final long GRIP_PATIENCE_MS = 5000;

	public String id;
	public Position position;
	public List<TimestampedFoodSource> foods;
	public Position assignedFood;

	@Local
	public State state;

	public Mode mode;

	@Local
	public CurrentTimeProvider clock;

	@Local
	public AntPlugin ant;

	@Local
	public Random rand;

	@Local
	public Position antHill;

	@Local
	public Long gripTimestamp;

	@Local
	public RuntimeLogger logger;

	/// Initial knowledge
	public AntComponent(int id, Random rand, DEECoNode node, Position antHill) {
		this.id = String.valueOf(id);
		this.rand = rand;
		this.clock = node.getRuntimeFramework().getScheduler().getTimer();
		this.ant = node.getPluginInstance(AntPlugin.class);
		this.foods = new LinkedList<>();
		this.state = State.Free;
		this.mode = Mode.Searching;
		this.antHill = antHill;
		this.logger = node.getRuntimeLogger();
	}

	/// Processes
	@Process
	@PeriodicScheduling(period = 500, order = 1)
	public static void sensePosition(@In("ant") AntPlugin ant, @Out("position") ParamHolder<Position> position) {
		position.value = ant.getPosition();
	}

	@Process
	@PeriodicScheduling(period = 500, order = 2)
	public static void senseState(@In("ant") AntPlugin ant, @Out("state") ParamHolder<State> state) {
		state.value = ant.getState();
	}

	@Process
	@PeriodicScheduling(period = 500, order = 3)
	public static void senseFood(@In("ant") AntPlugin ant, @In("clock") CurrentTimeProvider clock,
			@InOut("foods") ParamHolder<List<TimestampedFoodSource>> foods, @In("position") Position position) {
		// Remove old food
		Set<TimestampedFoodSource> toRemove = new LinkedHashSet<>();
		for (TimestampedFoodSource source : foods.value) {
			// Remove too old source data
			if (clock.getCurrentMilliseconds() - source.timestamp > MAX_FOOD_AGE_MS) {
				toRemove.add(source);
			}

			// Remove sources in range but not sensed (already collected sources)
			if (source.position.euclidDistanceTo(position) < AntPlugin.SENSE_RANGE_M) {
				boolean found = false;
				for (FoodSource sensed : ant.getSensedFood()) {
					if (PosUtils.isSame(sensed.position, source.position)) {
						found = true;
						break;
					}
				}
				if (!found) {
					toRemove.add(source);
				}
			}
		}
		foods.value.removeAll(toRemove);

		// Add new sensed food
		for (FoodSource newSource : ant.getSensedFood()) {
			// Try to update existing one
			boolean updated = false;
			for (TimestampedFoodSource oldSource : foods.value) {
				if (PosUtils.isSame(oldSource.position, newSource.position)) {
					oldSource.timestamp = clock.getCurrentMilliseconds();
					oldSource.portions = newSource.portions;
					updated = true;
				}
			}

			// Skip adding if update was performed
			if (updated) {
				continue;
			}

			// Add new source
			if (newSource.portions > 0) {
				foods.value.add(new TimestampedFoodSource(newSource, clock.getCurrentMilliseconds()));
			}
		}
	}

/*	@Process
	@PeriodicScheduling(period = 1000, order = 4)
	public static void log(@In("id") String id, @In("position") Position position, @In("foods") List<TimestampedFoodSource> foods) {
		if (position == null) {
			return;
		}

		try {
			ProcessContext.getRuntimeLogger().log(new AntLogRecord(id, position));
			FoodLogRecord.logAll(ProcessContext.getRuntimeLogger(), foods);
		} catch (Exception e) {
			throw new DEECoRuntimeException("Ant log failed with exception", e);
		}
	}*/

	@Process
	@PeriodicScheduling(period = 5000, order = 5)
	public static void printStatus(@In("clock") CurrentTimeProvider clock, @In("id") String id,
			@In("position") Position position, @In("state") State state, @In("mode") Mode mode,
			@In("foods") List<TimestampedFoodSource> foods, @In("assignedFood") Position assignedFood) {
		if (position == null) {
			return;
		}

		System.out.format("%06d: Ant %s, %s, %s, %s, foods: ", clock.getCurrentMilliseconds(), id, position, state,
				mode);

		for (TimestampedFoodSource source : foods) {
			System.out.format("%f, ", source.position.euclidDistanceTo(position));
		}

		System.out.print("assigned food: ");
		if (assignedFood != null) {
			System.out.print(assignedFood);
		} else {
			System.out.print("null");
		}

		System.out.println();
	}

	@Process
	@PeriodicScheduling(period = 1000, order = 6)
	public static void modeSwitch(@In("ant") AntPlugin ant, @InOut("mode") ParamHolder<Mode> mode,
			@In("assignedFood") Position assignedFood, @In("position") Position position,
			@In("clock") CurrentTimeProvider clock, @In("gripTimestamp") Long gripTimestamp) {
		switch (mode.value) {
		case Searching:
			if (assignedFood != null) {
				mode.value = Mode.ToFood;
			}
			break;
		case ToFood:
			// Cancel move to food
			if (assignedFood == null || PosUtils.isSame(assignedFood, ant.getTarget())) {
				mode.value = Mode.Searching;
			}
			// Grip the food
			// Needs to be sure, use sensed data instead of accumulated knowledge
			if (PosUtils.isSame(assignedFood, position)) {
				for (FoodSource source : ant.getSensedFood()) {
					if (PosUtils.isSame(source.position, assignedFood) && source.portions > 0) {
						mode.value = Mode.Grip;
					}
				}
			}
			break;
		case Grip:
			// Advance
			if (ant.getState() == State.Locked || ant.getState() == State.Pulling) {
				mode.value = Mode.Pulling;
			} else if (clock.getCurrentMilliseconds() - gripTimestamp > GRIP_PATIENCE_MS) {
				mode.value = Mode.Searching;
			}
			break;
		case Pulling:
			if (ant.getState() == State.Free) {
				mode.value = Mode.Searching;
			}
			break;
		}
	}

	@Process
	@PeriodicScheduling(period = 1000, order = 7)
	public static void move(@In("ant") AntPlugin ant, @In("mode") Mode mode, @In("rand") Random rand,
			@In("assignedFood") Position assignedFood, @Out("gripTimestamp") ParamHolder<Long> gripTimestamp,
			@In("clock") CurrentTimeProvider clock) {
		switch (mode) {
		case Searching:
			if (ant.isAtTarget()) {
				ant.setTarget(PosUtils.getRandomPosition(rand, 0, 0, RANDOM_WALK_DIAMETER));
			}
			break;
		case ToFood:
			if (assignedFood != null) {
				ant.setTarget(assignedFood);
			}
			break;
		case Grip:
			ant.grab();
			gripTimestamp.value = clock.getCurrentMilliseconds();
			break;
		case Pulling:
			// TODO: use ant hill position
			ant.setTarget(new Position(0, 0));
		}
	}
}
