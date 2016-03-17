package cz.cuni.mff.d3s.isola2016.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cz.cuni.mff.d3s.jdeeco.network.address.MANETBroadcastAddress;
import cz.cuni.mff.d3s.jdeeco.network.l1.L1Packet;
import cz.cuni.mff.d3s.jdeeco.network.l1.MANETReceivedInfo;
import cz.cuni.mff.d3s.jdeeco.network.l2.L2Packet;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.RebroadcastStrategy;

public class ProabilisticRebroadcastStrategy extends RebroadcastStrategy {
	public final static long SEEN_HISTORY_LENGTH_MS = 1500;
	public final static double STATIC_REBROADCAST_PROBABILITY = 0.95;
	public final static long STATIC_REBROADCAST_DEALY_MEAN_MS = 25;
	public final static long STATIC_REBROADCAST_DEALY_VARIANCE_MS = 5;
	
	private Map<String, Long> nodeToLastSeen = new HashMap<>();
	private Random rand = new Random(42);
	private double staticRebroadcastProbability;
	
	public ProabilisticRebroadcastStrategy() {
		this(STATIC_REBROADCAST_PROBABILITY);
	}
	
	public ProabilisticRebroadcastStrategy(double staticRebroadcastProbability) {
		this.staticRebroadcastProbability = staticRebroadcastProbability;
	}

	private void markNodeSeen(String address) {
		nodeToLastSeen.put(address, scheduler.getTimer().getCurrentMilliseconds());
	}

	private int getNodesInRange() {
		int inRange = 0;
		for (long lastSeen : nodeToLastSeen.values()) {
			if (scheduler.getTimer().getCurrentMilliseconds() - lastSeen < SEEN_HISTORY_LENGTH_MS) {
				inRange++;
			}
		}
		return inRange;
	}

	@Override
	public void processL2Packet(L2Packet packet) {
		// Get average RSSI and report neighbor nodes
		double rssiSum = 0;
		int pktCnt = 0;
		for (L1Packet l1 : packet.getReceivedInfo().srcFragments) {
			if (l1.receivedInfo instanceof MANETReceivedInfo) {
				MANETReceivedInfo info = (MANETReceivedInfo) l1.receivedInfo;
				MANETBroadcastAddress address = (MANETBroadcastAddress) info.srcAddress;
				markNodeSeen(address.getAddress());
				rssiSum += info.rssi;
				pktCnt++;
			}
		}
		final double rssiAvg = rssiSum / pktCnt;

		// If the packet is blocked by communication boundary
		if (isBounded(packet))
			return;

		// System.err.println("Nodes in range: " + getNodesInRange());

		int divider = Math.max(1, getNodesInRange());
		if (rand.nextDouble() > (staticRebroadcastProbability / divider)) {
			return;
		}

		// Calculate rebroadcast delay
		//double ratio = Math.min(1, Math.abs(Math.log(rssiAvg) / Math.log(RSSI_250m)));
		//long delayMs = 1 + (long) ((1 - ratio) * MAX_DELAY);
		
		long delayMs = STATIC_REBROADCAST_DEALY_MEAN_MS + (long) ((0.5 - rand.nextDouble()) * STATIC_REBROADCAST_DEALY_VARIANCE_MS);
		scheduleRebroadcast(packet, delayMs);
				
		// Rebroadcast packet now
//		layer2.sendL2Packet(packet, MANETBroadcastAddress.BROADCAST);
	}
}
