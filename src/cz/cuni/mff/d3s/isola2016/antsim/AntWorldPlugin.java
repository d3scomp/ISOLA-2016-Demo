package cz.cuni.mff.d3s.isola2016.antsim;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;
import cz.cuni.mff.d3s.deeco.scheduler.Scheduler;
import cz.cuni.mff.d3s.deeco.task.TimerTask;
import cz.cuni.mff.d3s.deeco.task.TimerTaskListener;
import cz.cuni.mff.d3s.isola2016.antsim.AntPlugin.State;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class AntWorldPlugin implements DEECoPlugin, TimerTaskListener {
	public static long SIM_STEP_MS = 100;

	public Position antHill;
	public int collectedFoodPieces = 0;
	public Collection<AntPlugin> ants = new HashSet<>();
	public Collection<FoodSource> foodSources = new HashSet<>();
	public Collection<FoodPiece> foodPieces = new HashSet<>();

	Map<FoodSource, Set<AntPlugin>> lockedAtSource = new HashMap<>();

	private boolean initialized = false;

	public AntWorldPlugin(Position antHill) {
		this.antHill = antHill;
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

	void registerAnt(AntPlugin ant) {
		ants.add(ant);
	}

	public void addFoodSource(FoodSource source) {
		lockedAtSource.put(source, new HashSet<>());
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
	public AntPlugin getHelper(AntPlugin ant) {
		for (AntPlugin helper : ants) {
			if (helper != ant && helper.state == State.Locked
					&& PosUtils.isSame(helper.currentPosition, ant.currentPosition)) {
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
	void removeFoodSource(FoodSource source) {
		if (source.portions != 0) {
			System.err.println("Removing nonempty food source at " + source.portions + " with " + source.portions
					+ " portions remaining");
		}

		for (AntPlugin ant : lockedAtSource.get(source)) {
			if (ant.state == State.Locked) {
				ant.state = State.Free;
			}
			foodSources.remove(source);
			lockedAtSource.remove(source);
		}
	}
	
	private void resolveLocked() {
		// Resolve Locked -> Pulling
		for (AntPlugin ant : ants) {
			// Ant not locked
			if (ant.state != State.Locked) {
				continue;
			}
			
			// Get locked source
			FoodSource source = getFoodSourceAt(ant.currentPosition);
			if(source == null) {
				ant.state = State.Free;
			}

			// There is no helper
			AntPlugin helper = getHelper(ant);
			if (helper == null) {
				continue;
			}

			// Create food piece and start polling
			if(--source.portions == 0) {
				removeFoodSource(source);
			}
			FoodPiece piece = new FoodPiece(ant.currentPosition, ant, helper);
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
		for(FoodPiece piece: foodPieces) {
			if(PosUtils.isSame(piece.position, antHill)) {
				System.err.println("Food piece delivered");
				for(AntPlugin puller: piece.pullers) {
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
		if(ant.currentTarget == null) {
			return;
		}
		
		// Get vector
		double dx = ant.currentTarget.x - ant.currentPosition.x;
		double dy = ant.currentTarget.y - ant.currentPosition.y;

		// Normalize
		double length = Math.sqrt(dx * dx + dy * dy);
		dx /= length;
		dy /= length;

		// Apply speed
		double multiplier = (AntPlugin.SPEED_M_PER_S * AntWorldPlugin.SIM_STEP_MS) / 1000;
		dx *= multiplier;
		dy *= multiplier;

		// Apply movement
		ant.currentPosition = new Position(ant.currentPosition.x + dx, ant.currentPosition.y + dy);
	}
	
	private void moveFood(FoodPiece piece) {
		double dx = 0;
		double dy = 0;

		// Get vector
		for(AntPlugin puller: piece.pullers) {
			dx += puller.currentTarget.x - puller.currentPosition.x;
			dy += puller.currentTarget.y - puller.currentPosition.y;
		}

		// Normalize
		double length = Math.sqrt(dx * dx + dy * dy);
		dx /= length;
		dy /= length;

		// Apply speed
		double multiplier = (AntPlugin.SPEED_M_PER_S * AntWorldPlugin.SIM_STEP_MS) / 1000;
		dx *= multiplier;
		dy *= multiplier;
		
		// Get average position of pulleys and piece
		double x = piece.position.x;
		double y = piece.position.y;
		for(AntPlugin puller: piece.pullers) {
			x += puller.currentPosition.x;
			y += puller.currentPosition.y;
		}
		x /= 1 + piece.pullers.size();
		y /= 1 + piece.pullers.size();
		
		// Move pulleys and piece
		Position newPos = new Position(x + dx, y + dy);
		for(AntPlugin puller: piece.pullers) {
			puller.currentPosition = newPos;
		}
		piece.position = newPos;
	}

	// Ant world simulation step
	@Override
	public void at(long time, Object triger) {
		// System.out.println("Ant world simulation step: " + time);
		resolveLocked();
		
		removeFoodAtHill();

		// Move free ants
		for (AntPlugin ant : ants) {
			if(ant.state == State.Free) {
				moveAnt(ant);
			}
		}

		// Resolve food movements
		for(FoodPiece piece: foodPieces) {
			moveFood(piece);
		}
	}
}
