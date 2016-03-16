package cz.cuni.mff.d3s.isola2016.antsim;

import java.util.Collection;

import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;

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
public class BigAntPlugin extends AntPlugin {
	public static enum State {
		Free, Locked, Pulling
	}
	
	public static double SENSE_RANGE_M = 3;
	// Realistic ant speed is 0.07 m/s
	// Mechanic ants are a lot faster
	public static double SPEED_M_PER_S = 0.5;

	public State state;
	public FoodPiece pulledFoodPiece;
	
	@Override
	public void init(DEECoContainer container) throws PluginInitFailedException {
		super.init(container);
		
		world.registerAnt(this);
	}
	
	public BigAntPlugin() {
		state = State.Free;
	}
	
	public Collection<FoodSource> getSensedFood() {
		return world.getSensedFood(position, SENSE_RANGE_M);
	}

	public void grab() {
		FoodSource source = world.getFoodSourceAt(position);
		if (source != null) {
			state = State.Locked;
			world.lockedAtSource.get(source).add(this);
		}
	}

	public void release() {
		if(state == State.Locked) {
			state = State.Free;
			if(pulledFoodPiece != null) {
				pulledFoodPiece.pullers.remove(this);
				pulledFoodPiece = null;
			}
		} else {
			Log.e("Releasing when not Locked, state: " + state);
		}
	}

	public State getState() {
		return state;
	}
}
