package cz.cuni.mff.d3s.isola2016.demo;

import java.rmi.UnexpectedException;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.simulation.omnet.OMNeTUtils;
import cz.cuni.mff.d3s.deeco.timer.DiscreteEventTimer;
import cz.cuni.mff.d3s.isola2016.antsim.AbstractAntWorldPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.BigAntPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.QuantumAntWorldPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.SmallAntPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.StandardAntWorldPlugin;
import cz.cuni.mff.d3s.isola2016.ensemble.AntAssignmetSolver;
import cz.cuni.mff.d3s.isola2016.ensemble.FitnessMode;
import cz.cuni.mff.d3s.isola2016.ensemble.HeuristicSolver;
import cz.cuni.mff.d3s.isola2016.ensemble.IntelligentAntPlanning;
import cz.cuni.mff.d3s.isola2016.ensemble.PairedHeuristicSolver;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.jdeeco.network.Network;
import cz.cuni.mff.d3s.jdeeco.network.device.SimpleBroadcastDevice;
import cz.cuni.mff.d3s.jdeeco.network.l1.strategy.L2PacketCounter;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.KnowledgeInsertingStrategy;
import cz.cuni.mff.d3s.jdeeco.network.omnet.OMNeTBroadcastDevice;
import cz.cuni.mff.d3s.jdeeco.network.omnet.OMNeTSimulation;
import cz.cuni.mff.d3s.jdeeco.position.Position;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;
import cz.cuni.mff.d3s.jdeeco.publishing.DefaultKnowledgePublisher;

class SolverFactory {
	final Config cfg;
	final AbstractAntWorldPlugin world;

	public SolverFactory(Config cfg, AbstractAntWorldPlugin world) {
		this.cfg = cfg;
		this.world = world;
	}

	public AntAssignmetSolver getSolver() {
		switch (cfg.mode) {
		case "standard":
			switch (cfg.fitnessType) {
			case "PreferClose":
				return new HeuristicSolver(FitnessMode.PreferCloseFoods, cfg, world);
			case "PreferDistant":
				return new HeuristicSolver(FitnessMode.PreferDistantFoods, cfg, world);
			case "PreferNeutral":
				return new HeuristicSolver(FitnessMode.PreferNeutral, cfg, world);
			default:
				throw new UnsupportedOperationException("Unknown fitness type \"" + cfg.fitnessType + "\"");
			}
		case "quantum":
			switch (cfg.fitnessType) {
			case "PreferClose":
				return new PairedHeuristicSolver(FitnessMode.PreferCloseFoods, cfg, world);
			case "PreferDistant":
				return new PairedHeuristicSolver(FitnessMode.PreferDistantFoods, cfg, world);
			case "PreferNeutral":
				return new PairedHeuristicSolver(FitnessMode.PreferNeutral, cfg, world);
			default:
				throw new UnsupportedOperationException("Unknown fitness type \"" + cfg.fitnessType + "\"");
			}
		default:
			throw new UnsupportedOperationException("Mode \"" + cfg.mode + "\" not defined");
		}
	}
}

public class DemoLauncher {
	public static final double ANT_SPAWN_DIAMETER_M = 10;
	public static final Position ANT_HILL_POS = new Position(0, 0);

	public static void main(String[] args) throws Exception {
		Config cfg = new Config(args);
		run(cfg);
	}

	public static void run(Config cfg) throws Exception {
		System.out.println("JAVA library path: " + System.getProperty("java.library.path"));
		System.out.println("Ant food picking simulation demo");

		// Pseudo random number generator
		// If passing this to components, just use this to generate seed for new generator
		Random rand = new Random(cfg.seed);

		// Define ant world
		AbstractAntWorldPlugin antWorld;
		switch (cfg.mode) {
		case "standard":
			antWorld = new StandardAntWorldPlugin(ANT_HILL_POS, new Random(rand.nextLong()), cfg);
			break;
		case "quantum":
			antWorld = new QuantumAntWorldPlugin(ANT_HILL_POS, new Random(rand.nextLong()), cfg);
		default:
			throw new UnexpectedException("Mode \"" + cfg.mode + "\" not defined");
		}

		// Create solver factory
		SolverFactory solverFactory = new SolverFactory(cfg, antWorld);

		// Define simulation
		DEECoSimulation realm;
		switch (cfg.networkModel) {
		case "simple":
			realm = new DEECoSimulation(new DiscreteEventTimer());
			realm.addPlugin(new SimpleBroadcastDevice(25, 10, cfg.radioRangeM, 1024));
			break;
		case "omnet":
			OMNeTSimulation omnetSim = new OMNeTSimulation();
			omnetSim.set80154txPower(OMNeTUtils.RangeToPower_802_15_4(cfg.radioRangeM));
			realm = new DEECoSimulation(omnetSim.getTimer());
			realm.addPlugin(omnetSim);
			realm.addPlugin(OMNeTBroadcastDevice.class);
			break;
		default:
			throw new UnsupportedOperationException("Network model " + cfg.networkModel + " not defined");
		}

		// Add common plugins
		realm.addPlugin(Network.class);
		realm.addPlugin(KnowledgeInsertingStrategy.class);
		realm.addPlugin(antWorld);
		L2PacketCounter counter = new L2PacketCounter();
		realm.addPlugin(counter);
		// realm.addPlugin(ProabilisticRebroadcastStrategy.class);
		// realm.addPlugin(KnowledgeSizeSampler.class);

		// Create big ant nodes
		int nodeCnt = 0;
		Set<DEECoNode> bigAnts = new LinkedHashSet<>();
		for (int i = 0; i < cfg.numBigAnts; ++i) {
			nodeCnt++;

			List<DEECoPlugin> plugins = new LinkedList<>();
			plugins.add(new DefaultKnowledgePublisher());
			plugins.add(new BigAntPlugin());
			plugins.add(new PositionPlugin(PosUtils.getRandomPosition(rand, 0, 0, ANT_SPAWN_DIAMETER_M)));
			plugins.add(new IntelligentAntPlanning(solverFactory.getSolver(), cfg.maxTimeSkewMs));
			if (cfg.useRebroadcasting) {
				plugins.add(new CachingRebroadcastStrategy(cfg.rebroadcastDelayMs, cfg.rebroadcastRangeM,
						cfg.maxTimeSkewMs));
			}

			DEECoNode node = realm.createNode(nodeCnt, plugins.toArray(new DEECoPlugin[] {}));

			switch (cfg.mode) {
			case "standard":
				node.deployComponent(new BigAntComponent(nodeCnt, new Random(rand.nextLong()), node, ANT_HILL_POS));
				break;
			case "quantum":
				node.deployComponent(new BigAntComponent(nodeCnt, new Random(rand.nextLong()), node, ANT_HILL_POS));
				break;
			default:
				throw new UnexpectedException("Mode \"" + cfg.mode + "\" not defined");
			}

			node.deployEnsemble(AntPosExchangeEnsemble.class);
			node.deployEnsemble(FoodSourceExchangeEnsemble.class);
			bigAnts.add(node);
		}

		// Create small ant nodes
		if (cfg.useRebroadcasting) {
			for (int i = 0; i < cfg.numSmallAnts; ++i) {
				nodeCnt++;

				List<DEECoPlugin> plugins = new LinkedList<>();
				plugins.add(new SmallAntPlugin());
				plugins.add(new PositionPlugin(PosUtils.getRandomPosition(rand, 0, 0, ANT_SPAWN_DIAMETER_M)));
				plugins.add(new CachingRebroadcastStrategy(cfg.rebroadcastDelayMs, cfg.rebroadcastRangeM,
						cfg.maxTimeSkewMs));

				DEECoNode node = realm.createNode(nodeCnt, plugins.toArray(new DEECoPlugin[] {}));

				node.deployComponent(new SmallAntComponent(nodeCnt, new Random(rand.nextLong()), node));
			}
		}

		// Run the simulation
		System.out.println("Running the simulation.");
		realm.start(cfg.limitMs);
		System.out.println("All done.");

		System.out.println("Total food pieces delivered: " + antWorld.collectedFoodPieces);
		System.out.println("Total packets: " + counter.toString());
	}
}
