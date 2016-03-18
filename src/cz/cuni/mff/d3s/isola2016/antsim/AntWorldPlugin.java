package cz.cuni.mff.d3s.isola2016.antsim;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;

import javax.xml.bind.JAXB;

import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;
import cz.cuni.mff.d3s.deeco.scheduler.Scheduler;
import cz.cuni.mff.d3s.deeco.task.TimerTask;
import cz.cuni.mff.d3s.deeco.task.TimerTaskListener;
import cz.cuni.mff.d3s.isola2016.antsim.BigAntPlugin.State;
import cz.cuni.mff.d3s.isola2016.demo.Config;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class AntWorldPlugin implements DEECoPlugin, TimerTaskListener {
	public static final long SIM_STEP_MS = 100;
	public static final double FOOD_SOURCE_SPAWN_DIAMETER_M = 15;
	public static final int FOOD_SOURCE_CAPACITY = 1;
	public static final double DEFAULT_SOURCE_APPEAR_PROBABILITY_PER_S = 1.0 / 15;
	public static final double DEFAULT_SOURCE_DISAPPEAR_PROBABILITY_PER_S = 1.0 / 20.0;

	public Position antHill;
	public int collectedFoodPieces = 0;
	public Collection<BigAntPlugin> bigAnts = new LinkedHashSet<>();
	public Collection<SmallAntPlugin> smallAnts = new LinkedHashSet<>();
	public Collection<FoodSource> foodSources = new LinkedHashSet<>();
	public Collection<FoodPiece> foodPieces = new LinkedHashSet<>();

	Map<FoodSource, Set<BigAntPlugin>> lockedAtSource = new HashMap<>();

	private boolean initialized = false;
	private final long startTime;
	private final Random rand;

	// Configuration object (this is here just to be serialized)
	public Config config;

	private double sourceAppearProbabilityPerSec = DEFAULT_SOURCE_APPEAR_PROBABILITY_PER_S;
	private double sourceDisappearProbabilityPerSec = DEFAULT_SOURCE_DISAPPEAR_PROBABILITY_PER_S;

	public AntWorldPlugin(Position antHill, Random rand) {
		this.antHill = antHill;
		this.rand = rand;
		startTime = System.currentTimeMillis();
	}

	@Override
	public List<Class<? extends DEECoPlugin>> getDependencies() {
		return new ArrayList<>();
	}

	@Override
	public void init(DEECoContainer container) throws PluginInitFailedException {
		if (!initialized) {
			initialized = true;
			Scheduler scheduler = container.getRuntimeFramework().getScheduler();
			new TimerTask(scheduler, this, "AntWorldSimStep", 0, SIM_STEP_MS).schedule();
		}
	}

	void registerAnt(BigAntPlugin ant) {
		bigAnts.add(ant);
	}

	void registerAnt(SmallAntPlugin ant) {
		smallAnts.add(ant);
	}

	public void addFoodSource(FoodSource source) {
		lockedAtSource.put(source, new LinkedHashSet<>());
		foodSources.add(source);
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
			if (PosUtils.isSame(source.position, position)) {
				return source;
			}
		}
		return null;
	}

	/**
	 * Returns helper ant
	 * 
	 */
	public BigAntPlugin getHelper(BigAntPlugin ant) {
		for (BigAntPlugin helper : bigAnts) {
			if (helper != ant && helper.state == State.Locked
					&& PosUtils.isSame(helper.getPosition(), ant.getPosition())) {
				return helper;
			}
		}
		return null;
	}

	/**
	 * Removes food source
	 * 
	 * @param source
	 */
	public void removeFoodSource(FoodSource source) {
		for (BigAntPlugin ant : lockedAtSource.get(source)) {
			if (ant.state == State.Locked) {
				ant.state = State.Free;
			}
		}

		foodSources.remove(source);
		lockedAtSource.remove(source);
	}

	private void resolveLocked() {
		// Resolve Locked -> Pulling
		for (BigAntPlugin ant : bigAnts) {
			// Ant not locked
			if (ant.state != State.Locked) {
				continue;
			}

			// Get locked source
			FoodSource source = getFoodSourceAt(ant.getPosition());
			if (source == null) {
				ant.state = State.Free;
			}

			// There is no helper
			BigAntPlugin helper = getHelper(ant);
			if (helper == null) {
				continue;
			}

			// Create food piece and start polling
			if (--source.portions == 0) {
				removeFoodSource(source);
			}
			FoodPiece piece = new FoodPiece(ant.getPosition(), ant, helper);
			foodPieces.add(piece);
			ant.state = State.Pulling;
			ant.pulledFoodPiece = piece;
			helper.state = State.Pulling;
			helper.pulledFoodPiece = piece;
		}
	}

	private void removeFoodAtHill() {
		// Remove food at hill
		Collection<FoodPiece> toRemove = new HashSet<>();
		for (FoodPiece piece : foodPieces) {
			if (PosUtils.isSame(piece.position, antHill)) {
				System.err.println("Food piece delivered");
				for (BigAntPlugin puller : piece.pullers) {
					puller.state = State.Free;
					puller.pulledFoodPiece = null;
				}
				toRemove.add(piece);
			}
		}
		collectedFoodPieces += toRemove.size();
		foodPieces.removeAll(toRemove);
	}

	private void moveAnt(AntPlugin ant) {
		Position target = ant.getTarget();

		// No target -> no move
		if (target == null) {
			return;
		}

		// Get movement speed
		double moveDistance = (AntPlugin.SPEED_M_PER_S * AntWorldPlugin.SIM_STEP_MS) / 1000;

		// Closer than movement distance -> move to target
		if (ant.getPosition().euclidDistanceTo(ant.getTarget()) < moveDistance) {
			ant.setPosition(new Position(target.x, target.y + 0.0001));
		} else {
			// Get vector
			double dx = target.x - ant.getPosition().x;
			double dy = target.y - ant.getPosition().y;

			// Normalize
			double length = Math.sqrt(dx * dx + dy * dy);
			dx /= length;
			dy /= length;

			// Apply speed
			dx *= moveDistance;
			dy *= moveDistance;

			// Apply movement
			ant.setPosition(new Position(ant.getPosition().x + dx, ant.getPosition().y + dy));
		}
	}

	private void moveFood(FoodPiece piece) {
		double dx = 0;
		double dy = 0;
		double tx = 0;
		double ty = 0;

		// Get vector
		for (AntPlugin puller : piece.pullers) {
			dx += puller.getTarget().x - puller.getPosition().x;
			dy += puller.getTarget().y - puller.getPosition().y;
			tx += puller.getTarget().x;
			ty += puller.getTarget().y;
		}
		tx /= piece.pullers.size();
		ty /= piece.pullers.size();

		// Normalize
		double length = Math.sqrt(dx * dx + dy * dy);
		dx /= length;
		dy /= length;

		// Apply speed
		double moveDistance = (AntPlugin.SPEED_M_PER_S * AntWorldPlugin.SIM_STEP_MS) / 1000;
		dx *= moveDistance;
		dy *= moveDistance;

		// Get average position of pulleys and piece
		double x = piece.position.x;
		double y = piece.position.y;
		for (AntPlugin puller : piece.pullers) {
			x += puller.getPosition().x;
			y += puller.getPosition().y;
		}
		x /= 1 + piece.pullers.size();
		y /= 1 + piece.pullers.size();

		// Closer to target than move distance
		Position target = new Position(tx, ty);
		if (target.euclidDistanceTo(new Position(x, y)) < moveDistance) {
			for (AntPlugin puller : piece.pullers) {
				puller.setPosition(target);
				piece.position = target;
			}
		} else {
			// Move pulleys and piece
			Position newPos = new Position(x + dx, y + dy);
			for (AntPlugin puller : piece.pullers) {
				puller.setPosition(newPos);
			}
			piece.position = newPos;
		}
	}

	public void log(long time) {
		File dir = new File(String.format("logs/world-%d/%09d", startTime, time / 60000));
		dir.mkdirs();
		File out = new File(String.format("%s/%09d.xml", dir.getAbsolutePath(), time));
		JAXB.marshal(this, out);
	}

	private void maintainFoodSourcePopulation() {
		// Add new food sources
		if (rand.nextDouble() < sourceAppearProbabilityPerSec / (1000 / SIM_STEP_MS)) {
			addFoodSource(new FoodSource(PosUtils.getRandomPosition(rand, antHill, FOOD_SOURCE_SPAWN_DIAMETER_M),
					FOOD_SOURCE_CAPACITY));
		}

		// Remove food sources
		List<FoodSource> toRemove = new LinkedList<>();
		for (FoodSource source : foodSources) {
			if (rand.nextDouble() < sourceDisappearProbabilityPerSec / (1000 / SIM_STEP_MS)) {
				toRemove.add(source);
			}
		}
		for (FoodSource source : toRemove) {
			removeFoodSource(source);
		}
	}

	// Ant world simulation step
	@Override
	public void at(long time, Object triger) {
		resolveLocked();

		removeFoodAtHill();

		maintainFoodSourcePopulation();

		// Move free ants
		for (BigAntPlugin ant : bigAnts) {
			if (ant.state == State.Free) {
				moveAnt(ant);
			}
			ant.updateAntInfo();
		}
		
		// Move small ants
		for (AntPlugin ant : smallAnts) {
			moveAnt(ant);
		}

		// Resolve food movements
		for (FoodPiece piece : foodPieces) {
			moveFood(piece);
		}

		// Log current state
		if (time % 1000 == 0) {
			log(time);
		}
	}
}
