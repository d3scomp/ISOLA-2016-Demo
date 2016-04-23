package cz.cuni.mff.d3s.isola2016.antsim;

import java.util.Collection;
import java.util.LinkedHashSet;

import cz.cuni.mff.d3s.deeco.knowledge.ReadOnlyKnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.container.ReadOnlyKnowledgeWrapper;
import cz.cuni.mff.d3s.deeco.logging.Log;
import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.runtime.DEECoRuntimeException;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;
import cz.cuni.mff.d3s.isola2016.demo.BigAnt;
import cz.cuni.mff.d3s.isola2016.demo.FoodSource;

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
	
	public static double SENSE_RANGE_M = 5;
	// Realistic ant speed is 0.07 m/s
	// Mechanic ants are a lot faster
	public static double SPEED_M_PER_S = 0.5;

	public State state;
	public FoodPiece pulledFoodPiece;
	public BigAnt antInfo;
	public LinkedHashSet<BigAnt> otherAntInfo;
	
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
	
	public void updateAntInfo() {
		try {
			ReadOnlyKnowledgeWrapper knowledgeWrapper = new ReadOnlyKnowledgeWrapper(container.getRuntimeFramework().getContainer().getLocals().iterator().next());
			try {
				antInfo = knowledgeWrapper.getUntrackedRoleKnowledge(BigAnt.class);
			} catch (Exception e) {
				throw new DEECoRuntimeException("Ant info knowledge extraction failed", e);
			}
			otherAntInfo = new LinkedHashSet<>();
			for (ReadOnlyKnowledgeManager remote : container.getRuntimeFramework().getContainer().getReplicas()) {
				ReadOnlyKnowledgeWrapper wrapper = new ReadOnlyKnowledgeWrapper(remote);
				otherAntInfo.add(wrapper.getUntrackedRoleKnowledge(BigAnt.class));
			}
		} catch (Exception e) {
			new DEECoRuntimeException("Knowledge exstraction for simulation dump failed", e);
		}
	}
}
