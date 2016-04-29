package cz.cuni.mff.d3s.isola2016.demo;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import cz.cuni.mff.d3s.deeco.model.runtime.custom.KnowledgePathExt;
import cz.cuni.mff.d3s.deeco.network.KnowledgeData;
import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.task.TimerTask;
import cz.cuni.mff.d3s.deeco.task.TimerTaskListener;
import cz.cuni.mff.d3s.jdeeco.network.address.MANETBroadcastAddress;
import cz.cuni.mff.d3s.jdeeco.network.l2.L2Packet;
import cz.cuni.mff.d3s.jdeeco.network.l2.strategy.RebroadcastStrategy;
import cz.cuni.mff.d3s.jdeeco.position.Position;
import cz.cuni.mff.d3s.jdeeco.position.PositionPlugin;

public class CachingRebroadcastStrategy extends RebroadcastStrategy implements TimerTaskListener {
	public static final long DEFAULT_PUBLISHING_PERIOD_MS = 2000;
	public static final double DEFAULT_BOUNDARY_RANGE_M = 10;
	public static final long DEFAULT_BOUNDARY_TIME_MS = 20000;
	
	private Map<Byte, L2Packet> cache = new HashMap<>();
	private PositionPlugin positionPlug;
	private final long publishingPeriodMs;
	private final double boundaryRangeM;
	private final long boundaryLatencyMs;
	
	public CachingRebroadcastStrategy() {
		publishingPeriodMs = DEFAULT_PUBLISHING_PERIOD_MS;
		boundaryRangeM = DEFAULT_BOUNDARY_RANGE_M;
		boundaryLatencyMs = DEFAULT_BOUNDARY_TIME_MS;
	}
	
	public CachingRebroadcastStrategy(long publishingPeriodMs, double boudaryRangeM, long boundaryTimeMs) {
		this.publishingPeriodMs = publishingPeriodMs;
		this.boundaryRangeM = boudaryRangeM;
		this.boundaryLatencyMs = boundaryTimeMs;
	}
	
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
		
		positionPlug = container.getPluginInstance(PositionPlugin.class);
		
		long offset = new Random(container.getId()).nextLong() % publishingPeriodMs;
		new TimerTask(scheduler, this, "Caching rebroadcast strategy publish", offset, publishingPeriodMs).schedule();
	}

	@Override
	public void at(long time, Object triger) {
		// Drop bounded data
		for(Iterator<Byte> i = cache.keySet().iterator(); i.hasNext();) {
			Byte key = i.next();
			L2Packet packet = cache.get(key);
			
			if(isBounded(packet) || customBounded(packet, time)) {
				i.remove();
			}
		}
		
		// Rebroadcast cache content
		for(L2Packet packet: cache.values()) {
			layer2.sendL2Packet(packet, MANETBroadcastAddress.BROADCAST);
		}
	}
	
	private boolean customBounded(L2Packet packet, long curTime) {
		KnowledgeData data = (KnowledgeData) packet.getObject();
		
		Position remotePosition = (Position) data.getKnowledge().getValue(KnowledgePathExt.createKnowledgePath("position"));
		Long remoteTime = (Long) data.getKnowledge().getValue(KnowledgePathExt.createKnowledgePath("time"));
		Position localPosition = positionPlug.getPosition();
		
		return localPosition.euclidDistanceTo(remotePosition) > boundaryRangeM && curTime - remoteTime > boundaryLatencyMs;
	}
}
