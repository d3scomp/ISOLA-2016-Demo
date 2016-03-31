package cz.cuni.mff.d3s.isola2016.demo;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.timer.DiscreteEventTimer;
import cz.cuni.mff.d3s.isola2016.antsim.AbstractAntWorldPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.BigAntPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.SmallAntPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.StandardAntWorldPlugin;
import cz.cuni.mff.d3s.isola2016.ensemble.AntAssignmetSolver;
import cz.cuni.mff.d3s.isola2016.ensemble.HeuristicSolver;
import cz.cuni.mff.d3s.isola2016.ensemble.IntelligentAntPlanning;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.jdeeco.network.Network;
import cz.cuni.mff.d3s.jdeeco.network.device.SimpleBroadcastDevice;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.KnowledgeInsertingStrategy;
import cz.cuni.mff.d3s.jdeeco.position.Position;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;
import cz.cuni.mff.d3s.jdeeco.publishing.DefaultKnowledgePublisher;

public class DemoLauncher {
	public static final double ANT_SPAWN_DIAMETER_M = 10;
	public static final Position ANT_HILL_POS = new Position(0, 0);
	public static final String LOG_PATH = "logs/runtime";

	public static void main(String[] args) throws Exception {
		Config cfg = new Config(args);
		run(cfg);
	}

	public static void run(Config cfg) throws Exception {
		System.out.println("Ant food picking simulation demo");

		DiscreteEventTimer discreteTimer = new DiscreteEventTimer();

		// OMNeTSimulation omnetSim = new OMNeTSimulation();
		// omnetSim.set80154txPower(OMNeTUtils.RangeToPower_802_15_4(cfg.radioRangeM));

		// DEECoSimulation realm = new DEECoSimulation(omnetSim.getTimer());
		DEECoSimulation realm = new DEECoSimulation(discreteTimer);

		Random rand = new Random(cfg.seed);
		AbstractAntWorldPlugin antWorld = new StandardAntWorldPlugin(ANT_HILL_POS, new Random(rand.nextLong()), cfg);

		// Add plugins
		// realm.addPlugin(omnetSim);
		realm.addPlugin(Network.class);
		realm.addPlugin(KnowledgeInsertingStrategy.class);
		realm.addPlugin(antWorld);
		// realm.addPlugin(OMNeTBroadcastDevice.class);
		realm.addPlugin(new SimpleBroadcastDevice(25, 10, cfg.radioRangeM, 1024));
	//	realm.addPlugin(ProabilisticRebroadcastStrategy.class);
	//	realm.addPlugin(KnowledgeSizeSampler.class);

		// Ensemble solver
		//AntAssignmetSolver solver = new BruteforceSolver();
		AntAssignmetSolver solver = new HeuristicSolver();
		// AntAssignmetSolver solver = new ProactiveSolver();

		// Create big ant nodes
		int  nodeCnt = 0;
		for (int i = 0; i < cfg.numBigAnts; ++i) {
			nodeCnt++;
			
			List<DEECoPlugin> plugins = new LinkedList<>();
			plugins.add(new DefaultKnowledgePublisher());
			plugins.add(new BigAntPlugin());
			plugins.add(new PositionPlugin(PosUtils.getRandomPosition(rand, 0, 0, ANT_SPAWN_DIAMETER_M)));
			plugins.add(new IntelligentAntPlanning(solver, cfg.maxTimeSkewMs));
			if(cfg.useRebroadcasting) {
				plugins.add(new CachingRebroadcastStrategy(cfg.rebroadcastDelayMs, cfg.rebroadcastRangeM));
			}
			
			DEECoNode node = realm.createNode(nodeCnt, plugins.toArray(new DEECoPlugin[]{}));
			
			node.deployComponent(new BigAntComponent(nodeCnt, new Random(rand.nextLong()), node, ANT_HILL_POS));
			
			node.deployEnsemble(AntPosExchangeEnsemble.class);
			node.deployEnsemble(FoodSourceExchangeEnsemble.class);
		}

		// Create small ant nodes
		if(cfg.useRebroadcasting) {
			for (int i = 0; i < cfg.numSmallAnts; ++i) {
				nodeCnt++;
				
				List<DEECoPlugin> plugins = new LinkedList<>();
				plugins.add(new SmallAntPlugin());
				plugins.add(new PositionPlugin(PosUtils.getRandomPosition(rand, 0, 0, ANT_SPAWN_DIAMETER_M)));
				plugins.add(new CachingRebroadcastStrategy(cfg.rebroadcastDelayMs, cfg.rebroadcastRangeM));
				
				DEECoNode node = realm.createNode(nodeCnt, plugins.toArray(new DEECoPlugin[]{}));
				
				node.deployComponent(new SmallAntComponent(nodeCnt, new Random(rand.nextLong()), node));
			}
		}

		// Run the simulation
		System.out.println("Running the simulation.");
		realm.start(cfg.limitMs);
		System.out.println("All done.");

		System.out.println("Total food pieces delivered: " + antWorld.collectedFoodPieces);
	}
}
