package cz.cuni.mff.d3s.isola2016.demo;

import java.util.Random;

import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.isola2016.antsim.AntPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.AntWorldPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;
import cz.cuni.mff.d3s.jdeeco.network.Network;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.KnowledgeInsertingStrategy;
import cz.cuni.mff.d3s.jdeeco.network.omnet.OMNeTBroadcastDevice;
import cz.cuni.mff.d3s.jdeeco.network.omnet.OMNeTSimulation;
import cz.cuni.mff.d3s.jdeeco.position.Position;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;
import cz.cuni.mff.d3s.jdeeco.publishing.DefaultKnowledgePublisher;

public class DemoLauncher {
	public static final int NUM_ANTS = 8;
	public static final int NUM_FOOD_SOURCES = 10;
	public static final int FOOD_SOURCE_CAPACITY = 5;
	public static final double ANT_SPAWN_DIAMETER_M = 15;
	public static final double FOOD_SOURCE_SPAWN_DIAMETER_M = 20;

	public static void main(String[] args) throws Exception {
		System.out.println("Ant food picking simulation demo");

		OMNeTSimulation omnetSim = new OMNeTSimulation();
		DEECoSimulation realm = new DEECoSimulation(omnetSim.getTimer());
		AntWorldPlugin antWorld = new AntWorldPlugin(0, 0); // Ant hill at 0,0
		Random rand = new Random(42);

		// Add food sources
		for (int i = 0; i < NUM_FOOD_SOURCES; ++i) {
			Position pos = getRandomPosition(rand, 0, 0, FOOD_SOURCE_SPAWN_DIAMETER_M);
			antWorld.addFoodSource(new FoodSource(pos, FOOD_SOURCE_CAPACITY));
		}

		// Add plugins
		realm.addPlugin(omnetSim);
		realm.addPlugin(Network.class);
		realm.addPlugin(DefaultKnowledgePublisher.class);
		realm.addPlugin(KnowledgeInsertingStrategy.class);
		realm.addPlugin(antWorld);

		// Create nodes
		for (int i = 0; i < NUM_ANTS; ++i) {
			OMNeTBroadcastDevice netDev = new OMNeTBroadcastDevice();
			PositionPlugin posPlug = new PositionPlugin(getRandomPosition(rand, 0, 0, ANT_SPAWN_DIAMETER_M));
			AntPlugin antPlug = new AntPlugin();
			DEECoNode node = realm.createNode(i, netDev, posPlug, antPlug);
			node.deployComponent(new AntComponent(i, omnetSim.getTimer(), antPlug));
		}

		// Run the simulation
		System.out.println("Running the simulation.");
		realm.start(600_000);
		System.out.println("All done.");
	}

	/**
	 * Generates random position in circle around base
	 * 
	 * @param rand
	 *            Random data source
	 * @param x
	 *            Base x coordinate
	 * @param y
	 *            Base y coordinate
	 * @param diameter
	 *            Max distance from base
	 * @return Generated position
	 */
	private static Position getRandomPosition(Random rand, double x, double y, double diameter) {
		double px = x + ((rand.nextDouble() - 0.5) * diameter);
		double py = y + ((rand.nextDouble() - 0.5) * diameter);
		return new Position(px, py);
	}
}
