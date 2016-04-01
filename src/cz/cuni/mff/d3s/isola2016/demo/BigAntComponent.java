package cz.cuni.mff.d3s.isola2016.demo;

import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
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
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.task.ProcessContext;
import cz.cuni.mff.d3s.deeco.timer.CurrentTimeProvider;
import cz.cuni.mff.d3s.isola2016.antsim.BigAntPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.BigAntPlugin.State;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.jdeeco.position.Position;

@Component
public class BigAntComponent {
	public static final long MAX_FOOD_AGE_MS = 30000;
	public static final double RANDOM_WALK_DIAMETER_M = 15;
	public static final double RANDOM_WALK_DIFF_M = 10;
	public static final double RANDOM_WALK_NOFEAR_M = 15;

	public String id;
	public Position position;
	public List<FoodSource> foods;
	public Position assignedFood;
	public Mode mode;
	public Long time;

	@Local
	public Map<String, Position> otherPos;

	@Local
	public State state;

	@Local
	public CurrentTimeProvider clock;

	@Local
	public BigAntPlugin ant;

	@Local
	public Random rand;

	@Local
	public Position antHill;

	public Position assistantPos;

	/// Initial knowledge
	public BigAntComponent(int id, Random rand, DEECoNode node, Position antHill) {
		this.id = String.valueOf(id);
		this.rand = rand;
		this.clock = node.getRuntimeFramework().getScheduler().getTimer();
		this.ant = node.getPluginInstance(BigAntPlugin.class);
		this.foods = new LinkedList<>();
		this.state = State.Free;
		this.mode = Mode.Searching;
		this.antHill = antHill;
		this.time = clock.getCurrentMilliseconds();
		this.otherPos = new LinkedHashMap<>();
	}

	/// Processes
	@Process
	@PeriodicScheduling(period = 500, order = 1)
	public static void senseTime(@Out("time") ParamHolder<Long> time) {
		time.value = ProcessContext.getTimeProvider().getCurrentMilliseconds();
	}

	@Process
	@PeriodicScheduling(period = 500, order = 1)
	public static void sensePosition(@In("ant") BigAntPlugin ant, @Out("position") ParamHolder<Position> position) {
		position.value = ant.getPosition();
	}

	@Process
	@PeriodicScheduling(period = 500, order = 2)
	public static void senseState(@In("ant") BigAntPlugin ant, @Out("state") ParamHolder<State> state) {
		state.value = ant.getState();
	}

	@Process
	@PeriodicScheduling(period = 500, order = 3)
	public static void senseFood(@In("ant") BigAntPlugin ant, @In("clock") CurrentTimeProvider clock,
			@InOut("foods") ParamHolder<List<FoodSource>> foods, @In("position") Position position) {
		// Remove old food
		Set<FoodSource> toRemove = new LinkedHashSet<>();
		for (FoodSource source : foods.value) {
			// Remove too old source data
			if (clock.getCurrentMilliseconds() - source.timestamp > MAX_FOOD_AGE_MS && source.portions != 0) {
				toRemove.add(source);
				System.err.println("TOO OLD FOOD REMOVED");
			}

			// Remove sources in range but not sensed (already collected sources)
			if (source.position.euclidDistanceTo(position) < BigAntPlugin.SENSE_RANGE_M && source.portions > 0) {
				boolean found = false;
				for (FoodSource sensed : ant.getSensedFood()) {
					if (PosUtils.isSame(sensed.position, source.position)) {
						found = true;
						break;
					}
				}
				if (!found) {
					source.timestamp = clock.getCurrentMilliseconds();
					source.portions = 0;
				}
			}
		}
		foods.value.removeAll(toRemove);

		// Add new sensed food
		for (FoodSource newSource : ant.getSensedFood()) {
			// Try to update existing one
			boolean updated = false;
			for (FoodSource oldSource : foods.value) {
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
				foods.value.add(newSource.cloneWithTimestamp(clock.getCurrentMilliseconds()));
			}
		}
	}

	/*
	 * @Process
	 * 
	 * @PeriodicScheduling(period = 1000, order = 4) public static void log(@In("id") String id, @In("position")
	 * Position position, @In("foods") List<TimestampedFoodSource> foods) { if (position == null) { return; }
	 * 
	 * try { ProcessContext.getRuntimeLogger().log(new AntLogRecord(id, position));
	 * FoodLogRecord.logAll(ProcessContext.getRuntimeLogger(), foods); } catch (Exception e) { throw new
	 * DEECoRuntimeException("Ant log failed with exception", e); } }
	 */

	@Process
	@PeriodicScheduling(period = 1000, order = 5)
	public static void printStatus(@In("clock") CurrentTimeProvider clock, @In("id") String id,
			@In("position") Position position, @In("state") State state, @In("mode") Mode mode,
			@In("foods") List<FoodSource> foods, @In("assignedFood") Position assignedFood) {
		if (position == null) {
			return;
		}

		System.out.format("%06d: Ant %s, %s, %s, %s, foods: ", clock.getCurrentMilliseconds(), id, position, state,
				mode);

		for (FoodSource source : foods) {
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
	@PeriodicScheduling(period = 500, order = 6)
	public static void modeSwitch(@In("ant") BigAntPlugin ant, @InOut("mode") ParamHolder<Mode> mode,
			@In("assignedFood") Position assignedFood, @In("position") Position position,
			@In("clock") CurrentTimeProvider clock) {
		switch (mode.value) {
		case Searching:
			if (assignedFood != null) {
				mode.value = Mode.ToFood;
			}
			break;
		case ToFood:
			// Cancel move to food
			if (assignedFood == null) {
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
				
				if(mode.value != Mode.Grip) {
					System.err.println("NOT SURE");
				}
			}
			break;
		case Grip:
			// Advance
			if (ant.getState() == State.Locked || ant.getState() == State.Pulling) {
				mode.value = Mode.Gripped;
			} else if (assignedFood == null || !PosUtils.isSame(assignedFood, position)) {
				mode.value = Mode.Searching;
			}
			break;
		case Gripped:
			if (ant.getState() == State.Pulling) {
				mode.value = Mode.Pulling;
			} else if (assignedFood == null) {
				mode.value = Mode.Release;
			}
		case Release:
			if (ant.getState() == State.Free) {
				mode.value = Mode.Searching;
			}
		case Pulling:
			// Cancel pull
			if (ant.getState() == State.Free) {
				mode.value = Mode.Searching;
			}
			break;
		}
	}

	@Process
	@PeriodicScheduling(period = 500, order = 7)
	public static void move(@In("ant") BigAntPlugin ant, @In("mode") Mode mode, @In("rand") Random rand,
			@In("assignedFood") Position assignedFood, @In("clock") CurrentTimeProvider clock,
			@In("otherPos") Map<String, Position> otherPos) {
		switch (mode) {
		case Searching:
			if (ant.isAtTarget()) {
				ant.setTarget(getRandomWalkPos(otherPos.values(), ant.getPosition(), rand));
			}
			break;
		case ToFood:
			if (assignedFood != null) {
				ant.setTarget(assignedFood);
			}
			break;
		case Grip:
			ant.grab();
			break;
		case Gripped:
			break;
		case Release:
			ant.release();
		case Pulling:
			// TODO: use ant hill position
			ant.setTarget(new Position(0, 0));
		}
	}

	/**
	 * Picks N random close targets and uses the one which is the most distant from all other
	 */
	private static Position getRandomWalkPos(Collection<Position> others, Position current, Random rand) {
		Position result = null;
		double colDist = 0;
		int rounds = 10;
		while (rounds > 0 && (result == null || result.euclidDistanceTo(new Position(0, 0)) > RANDOM_WALK_DIAMETER_M)) {
			result = null;
			for (int i = 0; i < rounds; ++i) {
				Position newPos = PosUtils.getRandomPosition(rand, current, RANDOM_WALK_DIFF_M);
				double newDist = collectiveDistance(others, newPos);
				if (result == null || newDist > colDist) {
					result = newPos;
					colDist = newDist;
				}
			}
			rounds--;
		}
		if (rounds == 0) {
			result = PosUtils.getRandomPosition(rand, new Position(0, 0), RANDOM_WALK_DIAMETER_M);
		}
		return result;
	}

	private static double collectiveDistance(Collection<Position> collective, Position sample) {
		double res = 0;
		for (Position pos : collective) {
			double dist = pos.euclidDistanceTo(sample);
			if (dist < RANDOM_WALK_NOFEAR_M)
				res += dist;
		}
		return res;
	}
}
