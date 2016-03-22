package cz.cuni.mff.d3s.isola2016.demo;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.task.TimerTask;
import cz.cuni.mff.d3s.deeco.task.TimerTaskListener;
import cz.cuni.mff.d3s.jdeeco.network.address.MANETBroadcastAddress;
import cz.cuni.mff.d3s.jdeeco.network.l2.L2Packet;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.RebroadcastStrategy;

public class CachingRebroadcastStrategy extends RebroadcastStrategy implements TimerTaskListener {
	public static final int PUBLISHING_PERIOD = 2000;
	
	private Map<Byte, L2Packet> cache = new HashMap<>();
	
	@Override
	public void processL2Packet(L2Packet packet) {
		// Add or update cache record
		L2Packet cacheRecord = cache.get(packet.getReceivedInfo().srcNode);
		if(cacheRecord == null || cacheRecord.getReceivedInfo().dataId < packet.getReceivedInfo().dataId) {
			cache.put(packet.getReceivedInfo().srcNode, packet);
		}
	}
	
	@Override
	public void init(DEECoContainer container) {
		super.init(container);
		
		long offset = new Random(container.getId()).nextInt(PUBLISHING_PERIOD);
		new TimerTask(scheduler, this, "Caching rebroadcast strategy publish", offset, PUBLISHING_PERIOD).schedule();
	}

	@Override
	public void at(long time, Object triger) {
		for(L2Packet packet: cache.values()) {
			if(!isBounded(packet)) {
				layer2.sendL2Packet(packet, MANETBroadcastAddress.BROADCAST);
			}
		}
	}
}
