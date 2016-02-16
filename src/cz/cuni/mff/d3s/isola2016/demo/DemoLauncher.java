package cz.cuni.mff.d3s.isola2016.demo;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.isola2016.antsim.AntPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.AntWorldPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;
import cz.cuni.mff.d3s.isola2016.ensemble.IntelligentAntPlanning;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.isola2016.utils.SimpleLogger;
import cz.cuni.mff.d3s.jdeeco.network.Network;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.KnowledgeInsertingStrategy;
import cz.cuni.mff.d3s.jdeeco.network.omnet.OMNeTBroadcastDevice;
import cz.cuni.mff.d3s.jdeeco.network.omnet.OMNeTSimulation;
import cz.cuni.mff.d3s.jdeeco.position.Position;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;
import cz.cuni.mff.d3s.jdeeco.publishing.DefaultKnowledgePublisher;

public class DemoLauncher {
	public static final int NUM_ANTS = 6;
	public static final int NUM_FOOD_SOURCES = 4;
	public static final int FOOD_SOURCE_CAPACITY = 3;
	public static final double ANT_SPAWN_DIAMETER_M = 5;
	public static final double FOOD_SOURCE_SPAWN_DIAMETER_M = 15;
	public static final Position ANT_HILL_POS = new Position(0, 0);

	public static void main(String[] args) throws Exception {
		System.out.println("Ant food picking simulation demo");

		OMNeTSimulation omnetSim = new OMNeTSimulation();
		DEECoSimulation realm = new DEECoSimulation(omnetSim.getTimer());
		
		AntWorldPlugin antWorld = new AntWorldPlugin(ANT_HILL_POS);
		Random rand = new Random(42);
		
		// TODO: Do this properly
		SimpleLogger.clock = omnetSim.getTimer();

		// Add food sources
		for (int i = 0; i < NUM_FOOD_SOURCES; ++i) {
			Position pos = PosUtils.getRandomPosition(rand, 0, 0, FOOD_SOURCE_SPAWN_DIAMETER_M);
			antWorld.addFoodSource(new FoodSource(pos, FOOD_SOURCE_CAPACITY));
		}

		// Add plugins
		realm.addPlugin(omnetSim);
		realm.addPlugin(Network.class);
		realm.addPlugin(DefaultKnowledgePublisher.class);
		realm.addPlugin(KnowledgeInsertingStrategy.class);
		realm.addPlugin(antWorld);
		realm.addPlugin(AntPlugin.class);
		realm.addPlugin(OMNeTBroadcastDevice.class);
		realm.addPlugin(IntelligentAntPlanning.class);

		// Create nodes
		List<DEECoNode> nodes = new ArrayList<>();
		for (int i = 0; i < NUM_ANTS; ++i) {
			DEECoNode node = realm.createNode(i, new PositionPlugin(PosUtils.getRandomPosition(rand, 0, 0, ANT_SPAWN_DIAMETER_M)));
			node.deployComponent(new AntComponent(i, rand, node, ANT_HILL_POS));
			nodes.add(node);
		}

		// Run the simulation
		System.out.println("Running the simulation.");
		realm.start(1500_000);
		System.out.println("All done.");
		
		System.out.println("Total food pieced delivered: " + antWorld.collectedFoodPieces + " out of " + NUM_FOOD_SOURCES * FOOD_SOURCE_CAPACITY);
		for(AntPlugin ant: antWorld.ants) {
			System.out.println("Ant traveled distance: " + ant.totalTraveledDistance + " meters");
		}	
		
		// Finalize log, this should be fixed in log
		for(DEECoNode node: nodes) {
			node.getRuntimeLogger().flush();
		}
		
		SimpleLogger.close();
	}
}
