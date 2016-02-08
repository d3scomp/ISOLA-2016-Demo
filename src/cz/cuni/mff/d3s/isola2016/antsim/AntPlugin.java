package cz.cuni.mff.d3s.isola2016.antsim;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;
import cz.cuni.mff.d3s.jdeeco.position.Position;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;
import cz.cuni.mff.d3s.jdeeco.position.PositionProvider;

/**
 * Represents one ant in simulation
 * 
 * Grabbing logic: Ant can grab with grabber. When grab is attempted at food source it will succeed. After successful
 * grab the ant cannot move until two ants grab the same food source and create food piece. Then the two ants can move
 * the piece as long as they share the destination (there is no point to set other destination than ant hill).
 * 
 * @author Vladimir Matena <matena@d3s.mff.cuni.cz>
 *
 */
public class AntPlugin implements DEECoPlugin, PositionProvider {
	static enum State {
		Free, Locked, Pulling
	}

	public static double SENSE_RANGE_M = 1.5;
	public static double SPEED_M_PER_S = 0.03;

	private AntWorldPlugin world;

	State state;
	Position currentPosition;
	Position currentTarget;
	FoodPiece pulledFoodPiece;

	@Override
	public List<Class<? extends DEECoPlugin>> getDependencies() {
		return Arrays.asList(AntWorldPlugin.class, PositionPlugin.class);
	}

	@Override
	public void init(DEECoContainer container) throws PluginInitFailedException {
		state = State.Free;

		// Get world plugin reference
		world = container.getPluginInstance(AntWorldPlugin.class);
		world.registerAnt(this);

		// Set initial position and provide current position
		PositionPlugin positionPlugin = container.getPluginInstance(PositionPlugin.class);
		currentPosition = positionPlugin.getStaticPosition();
		positionPlugin.setProvider(this);
	}

	Collection<FoodSource> getSensedFood() {
		return world.getSensedFood(currentPosition, SENSE_RANGE_M);
	}

	public Position getPosition() {
		return currentPosition;
	}

	public void setTarget(Position target) {
		currentTarget = target;
	}

	public boolean grab() {
		if (world.getFoodSourceAt(currentPosition) == null) {
			return false;
		} else {
			state = State.Locked;
			return true;
		}
	}

	public void release() {
		state = State.Free;
		pulledFoodPiece.pullers.remove(this);
		pulledFoodPiece = null;
	}

	public State getState() {
		return state;
	}

	void update() {
		if (state == State.Free || state == State.Pulling) {
			move();
		}
	}

	private void move() {
		if(currentTarget == null) {
			return;
		}
		
		// Get vector
		double dx = currentTarget.x - currentPosition.x;
		double dy = currentTarget.y - currentPosition.y;

		// Normalize
		double length = Math.sqrt(dx * dx + dy * dy);
		dx /= length;
		dy /= length;

		// Apply speed
		double multiplier = (SPEED_M_PER_S * AntWorldPlugin.SIM_STEP_MS) / 1000;
		dx *= multiplier;
		dy *= multiplier;

		// Apply movement
		currentPosition = new Position(currentPosition.x + dx, currentPosition.y + dy);
	}
}
