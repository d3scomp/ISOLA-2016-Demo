package cz.cuni.mff.d3s.isola2016.antsim;

import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
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
	public static double SPEED_M_PER_S = 0.5;

	protected AntWorldPlugin world;

	public Position position;
	private Position target;
	public double totalTraveledDistance = 0;

	@Override
	public List<Class<? extends DEECoPlugin>> getDependencies() {
		return Arrays.asList(AntWorldPlugin.class, PositionPlugin.class);
	}

	@Override
	public void init(DEECoContainer container) throws PluginInitFailedException {
		// Get world plugin reference
		world = container.getPluginInstance(AntWorldPlugin.class);
		
		// Set initial position and provide current position
		PositionPlugin positionPlugin = container.getPluginInstance(PositionPlugin.class);
		position = positionPlugin.getStaticPosition();
		positionPlugin.setProvider(this);
	}

	public Position getPosition() {
		return position;
	}
	
	void setPosition(Position position) {
		totalTraveledDistance += position.euclidDistanceTo(this.position);
		this.position = position;
	}

	public void setTarget(Position target) {
		this.target = target;
	}
	
	public Position getTarget() {
		return target;
	}
	
	public boolean isAtTarget() {
		if(target == null) {
			return true;
		}
		
		return PosUtils.isSame(target, position);
	}
}
