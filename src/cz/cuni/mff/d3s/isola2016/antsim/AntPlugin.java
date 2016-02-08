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

public class AntPlugin implements DEECoPlugin, PositionProvider {
	public static double SENSE_RANGE_M = 1.5;
	
	private AntWorldPlugin world;
	
	Position currentPosition;
	FoodPiece pulledFoodPiece;
	
	public AntPlugin() {
		// TODO Auto-generated constructor stub
	}
	
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
		currentPosition = positionPlugin.getStaticPosition();
		positionPlugin.setProvider(this);
	}
	
	Collection<FoodSource> getSensedFood() {
		return null;
	}
	
	public Position getPosition() {
		return currentPosition;
	}
	
	void setTarget(Position position) {
		
	}
	
	void grab() {
		
	}
	
	void release() {
		
	}
	
	boolean isPulling() {
		return false;
	}
}
