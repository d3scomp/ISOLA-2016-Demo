package cz.cuni.mff.d3s.isola2016.demo;

import java.util.HashMap;

import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogRecord;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public final class AntLogRecord extends RuntimeLogRecord {
	static final class XMLPosition {
		private Position position;
		
		public XMLPosition(Position position) {
			this.position = position;
		}
		
		@Override
		public String toString() {
			return String.format("<position x=\"%f\" y=\"%f\" z=\"%f\"/>", position.x, position.y, position.z);
		}
	}
	
	@SuppressWarnings("serial")
	static final class AntLogMap extends HashMap<String, Object> {
		public AntLogMap(Position position) {
			this.put("positon", new XMLPosition(position));
		}
	}
	
	public AntLogRecord(String id, Position position) {
		super(AntComponent.class.getSimpleName(), new AntLogMap(position));
	}
}
