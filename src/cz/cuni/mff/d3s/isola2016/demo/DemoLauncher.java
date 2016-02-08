package cz.cuni.mff.d3s.isola2016.demo;

import java.util.Random;

import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.isola2016.antsim.AntPlugin;
import cz.cuni.mff.d3s.isola2016.antsim.AntWorldPlugin;
import cz.cuni.mff.d3s.jdeeco.network.Network;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.KnowledgeInsertingStrategy;
import cz.cuni.mff.d3s.jdeeco.network.omnet.OMNeTBroadcastDevice;
import cz.cuni.mff.d3s.jdeeco.network.omnet.OMNeTSimulation;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;
import cz.cuni.mff.d3s.jdeeco.publishing.DefaultKnowledgePublisher;

public class DemoLauncher {
	public static final int NUM_ANTS = 8;
	public static final double ANT_SPAWN_DIAMETER_M = 15;

	public static void main(String[] args) throws Exception {
		System.out.println("Ant food picking simulation demo");

		OMNeTSimulation omnetSim = new OMNeTSimulation();
		DEECoSimulation realm = new DEECoSimulation(omnetSim.getTimer());
		Random rand = new Random(42);

		realm.addPlugin(omnetSim);
		realm.addPlugin(Network.class);
		realm.addPlugin(DefaultKnowledgePublisher.class);
		realm.addPlugin(KnowledgeInsertingStrategy.class);
		realm.addPlugin(new AntWorldPlugin());

		for (int i = 0; i < NUM_ANTS; ++i) {
			OMNeTBroadcastDevice netDev = new OMNeTBroadcastDevice();
			PositionPlugin posPlug = new PositionPlugin((rand.nextDouble() - 0.5) * ANT_SPAWN_DIAMETER_M,
					(rand.nextDouble() - 0.5) * ANT_SPAWN_DIAMETER_M);
			AntPlugin antPlug = new AntPlugin();
			DEECoNode node = realm.createNode(i, netDev, posPlug, antPlug);
			node.deployComponent(new AntComponent(i, omnetSim.getTimer(), antPlug));
		}

		// Run the simulation
		System.out.println("Running the simulation.");
		realm.start(600_000);
		System.out.println("All done.");
	}

}
