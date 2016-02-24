package cz.cuni.mff.d3s.isola2016.utils;

import java.util.HashMap;

import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogRecord;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public final class AntLogRecord extends RuntimeLogRecord {
	@SuppressWarnings("serial")
	private static final class AntLogMap extends HashMap<String, Object> {
		public AntLogMap(Position position) {
			this.put("positon", new XMLPosition(position));
		}
	}
	
	public XMLPosition xmlPos;
	
	public AntLogRecord(String id, Position position) {
		super(id, new AntLogMap(position));
		xmlPos = new XMLPosition(position);
	}
}
