package cz.cuni.mff.d3s.isola2016.antsim;

import java.io.File;
import java.util.Arrays;
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
import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer.ShutdownListener;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;
import cz.cuni.mff.d3s.deeco.scheduler.Scheduler;
import cz.cuni.mff.d3s.deeco.task.TimerTask;
import cz.cuni.mff.d3s.deeco.task.TimerTaskListener;
import cz.cuni.mff.d3s.isola2016.antsim.BigAntPlugin.State;
import cz.cuni.mff.d3s.isola2016.demo.Config;
import cz.cuni.mff.d3s.isola2016.demo.FoodSource;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.jdeeco.network.l1.strategy.L2PacketCounter;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public abstract class AbstractAntWorldPlugin implements DEECoPlugin, TimerTaskListener {
	static class FinalLog {
		static class Report {
			public final L2PacketCounter msgCounter;
			public final int collected;
			public final double utility;
			
			Report(AbstractAntWorldPlugin world) {
				this.collected = world.collectedFoodPieces;
				this.msgCounter = world.counter;
				
				Double utility = 0.0;
				for(BigAntPlugin ant: world.bigAnts) {
					utility += ant.antInfo.totalUtility;
				}
				this.utility = utility;
			}
		}
		
		public final Config config;
		public final Report report;
		
		public FinalLog(AbstractAntWorldPlugin world) {
			this.config = world.config;
			this.report = new Report(world);
		}
	}

	public static final long SIM_STEP_MS = 100;
	public static final double FOOD_SOURCE_SPAWN_DIAMETER_M = 15;
	public static final int FOOD_SOURCE_CAPACITY = 1;
	public static final int HELPERS_NEEDED = 1;

	public Position antHill;
	public int collectedFoodPieces = 0;
	public double utility = 0;
	public Collection<BigAntPlugin> bigAnts = new LinkedHashSet<>();
	public Collection<SmallAntPlugin> smallAnts = new LinkedHashSet<>();
	public Collection<FoodSource> foodSources = new LinkedHashSet<>();
	public Collection<FoodPiece> foodPieces = new LinkedHashSet<>();

	Map<FoodSource, Set<BigAntPlugin>> lockedAtSource = new HashMap<>();

	private boolean initialized = false;
	private final long startTime;
	protected final Random rand;
	private L2PacketCounter counter;

	// Configuration object
	protected final Config config;

	public AbstractAntWorldPlugin(Position antHill, Random rand, Config config) {
		this.antHill = antHill;
		this.rand = rand;
		this.config = config;
		startTime = System.currentTimeMillis();
	}

	@Override
	public List<Class<? extends DEECoPlugin>> getDependencies() {
		return Arrays.asList(L2PacketCounter.class);
	}

	@Override
	public void init(DEECoContainer container) throws PluginInitFailedException {
		if (!initialized) {
			initialized = true;
			counter = container.getPluginInstance(L2PacketCounter.class);
			Scheduler scheduler = container.getRuntimeFramework().getScheduler();
			new TimerTask(scheduler, this, "AntWorldSimStep", 0, SIM_STEP_MS).schedule();
			
			// Schedule final low write
			container.addShutdownListener(new ShutdownListener() {
				@Override
				public void onShutdown() {
					finalLog();
				}
			});
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

	protected abstract void resolveLocked();
	
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
		double moveDistance = (AntPlugin.SPEED_M_PER_S * AbstractAntWorldPlugin.SIM_STEP_MS) / 1000;

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
		for (BigAntPlugin puller : piece.pullers) {
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
		double moveDistance = (AntPlugin.SPEED_M_PER_S * AbstractAntWorldPlugin.SIM_STEP_MS) / 1000;
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

	public void finalLog() {
		File dir = new File(String.format("logs/world-%d/", startTime));
		dir.mkdirs();
		File out = new File(String.format("%s/final.xml", dir.getAbsolutePath()));
		FinalLog log = new FinalLog(this);
		JAXB.marshal(log, out);
	}
	
	protected abstract void maintainFoodSourcePopulation();

	/**
	 * Move free ants
	 */
	private void moveFreeAnts() {
		for (BigAntPlugin ant : bigAnts) {
			if (ant.state == State.Free) {
				moveAnt(ant);
			}
			ant.updateAntInfo();
		}
	}

	/**
	 * Move small ants
	 */
	private void moveSmallAnts() {
		for (AntPlugin ant : smallAnts) {
			moveAnt(ant);
		}
	}

	/**
	 * Move food and pullers
	 */
	private void moveFoodAndPullers() {
		for (FoodPiece piece : foodPieces) {
			moveFood(piece);
		}
	}

	// Ant world simulation step
	@Override
	public void at(long time, Object triger) {
		resolveLocked();
		removeFoodAtHill();
		maintainFoodSourcePopulation();
		moveFreeAnts();
		moveSmallAnts();
		moveFoodAndPullers();

		// Log current state
		if(config.logIntervalMs != 0 && time % config.logIntervalMs == 0) {
			log(time);
		}
	}
}
