package cz.cuni.mff.d3s.isola2016.demo;

import java.util.Locale;
import java.util.Random;

import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogRecord;
import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogWriters;
import cz.cuni.mff.d3s.deeco.runtimelog.SnapshotProvider;
import cz.cuni.mff.d3s.deeco.simulation.omnet.OMNeTUtils;
import cz.cuni.mff.d3s.isola2016.antsim.AntPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.AntWorldPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;
import cz.cuni.mff.d3s.isola2016.ensemble.AntAssignmetSolver;
import cz.cuni.mff.d3s.isola2016.ensemble.HeuristicSolver;
import cz.cuni.mff.d3s.isola2016.ensemble.IntelligentAntPlanning;
import cz.cuni.mff.d3s.isola2016.utils.FoodLogRecord;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.jdeeco.network.Network;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.KnowledgeInsertingStrategy;
import cz.cuni.mff.d3s.jdeeco.network.omnet.OMNeTBroadcastDevice;
import cz.cuni.mff.d3s.jdeeco.network.omnet.OMNeTSimulation;
import cz.cuni.mff.d3s.jdeeco.position.Position;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;
import cz.cuni.mff.d3s.jdeeco.publishing.DefaultKnowledgePublisher;

public class DemoLauncher {
	public static final double RADIO_RANGE_M = 250;
	public static final int SEED = 42;
	public static final int NUM_ANTS = 10;
	public static final int NUM_FOOD_SOURCES = 20;
	public static final int FOOD_SOURCE_CAPACITY = 1;
	public static final double ANT_SPAWN_DIAMETER_M = 5;
	public static final double FOOD_SOURCE_SPAWN_DIAMETER_M = 15;
	public static final Position ANT_HILL_POS = new Position(0, 0);
	public static final String LOG_PATH = "logs/runtime";
	public static final long LIMIT_MS = 1500_000;

	public static void main(String[] args) throws Exception {
		run(SEED, LIMIT_MS, NUM_ANTS, NUM_FOOD_SOURCES, FOOD_SOURCE_CAPACITY, RADIO_RANGE_M);
	}

	public static void run(int seed, long limitMs, int numAnts, int numFoodSources, int foodSourceCapacity,
			double radioRangeM) throws Exception {
		System.out.println("Ant food picking simulation demo");

		// Setup logging directory
		final String logPath = String.format(Locale.US, "%s_seed-%d_ants-%d_foods-%d_capacity-%d_range-%02f", LOG_PATH,
				seed, numAnts, numFoodSources, foodSourceCapacity, radioRangeM);
		final RuntimeLogWriters logWriters = new RuntimeLogWriters(logPath);

		OMNeTSimulation omnetSim = new OMNeTSimulation();
		omnetSim.set80154txPower(OMNeTUtils.RangeToPower_802_15_4(radioRangeM));

		DEECoSimulation realm = new DEECoSimulation(omnetSim.getTimer());

		AntWorldPlugin antWorld = new AntWorldPlugin(ANT_HILL_POS);
		Random rand = new Random(seed);

		// Add food sources
		for (int i = 0; i < numFoodSources; ++i) {
			Position pos = PosUtils.getRandomPosition(rand, 0, 0, FOOD_SOURCE_SPAWN_DIAMETER_M);
			antWorld.addFoodSource(new FoodSource(pos, foodSourceCapacity));
		}

		// Add plugins
		realm.addPlugin(omnetSim);
		realm.addPlugin(Network.class);
		realm.addPlugin(DefaultKnowledgePublisher.class);
		realm.addPlugin(KnowledgeInsertingStrategy.class);
		realm.addPlugin(antWorld);
		realm.addPlugin(AntPlugin.class);
		realm.addPlugin(OMNeTBroadcastDevice.class);
		
		// Ensemble solver
		//AntAssignmetSolver solver = new BruteforceSolver();
		AntAssignmetSolver solver = new HeuristicSolver();
				
		// Create nodes
		for (int i = 0; i < numAnts; ++i) {
			DEECoNode node = realm.createNode(i, logWriters,
					new PositionPlugin(PosUtils.getRandomPosition(rand, 0, 0, ANT_SPAWN_DIAMETER_M)),
					new IntelligentAntPlanning(solver));
			node.deployComponent(new AntComponent(i, new Random(rand.nextLong()), node, ANT_HILL_POS));
			node.getRuntimeLogger().registerSnapshotProvider(new SnapshotProvider() {
				@Override
				public RuntimeLogRecord getSnapshot() {
					return new FoodLogRecord(antWorld.foodSources.iterator().next());
				}
				
				@Override
				public Class<? extends RuntimeLogRecord> getRecordClass() {
					return FoodLogRecord.class;
				}
			}, 5000);
		}
		
		// Run the simulation
		System.out.println("Running the simulation.");
		realm.start(limitMs);
		System.out.println("All done.");

		System.out.println("Total food pieced delivered: " + antWorld.collectedFoodPieces + " out of "
				+ numFoodSources * foodSourceCapacity);
		double totalDistance = 0;
		for (AntPlugin ant : antWorld.ants) {
			System.out.println("Ant traveled distance: " + ant.totalTraveledDistance + " meters");
			totalDistance += ant.totalTraveledDistance;
		}
		System.out.println("Total distance traveled: " + totalDistance);
	}
}
