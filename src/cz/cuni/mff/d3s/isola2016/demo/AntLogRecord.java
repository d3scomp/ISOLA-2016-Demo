package cz.cuni.mff.d3s.isola2016.demo;

import java.util.HashMap;
import java.util.Locale;

import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogRecord;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public final class AntLogRecord extends RuntimeLogRecord {
	public static final class XMLPosition {
		private Position position;
		
		public XMLPosition(Position position) {
			this.position = position;
		}
		
		@Override
		public String toString() {
			return String.format(Locale.US, "<x>%f</x><y>%f</y><z>%f</z>", position.x, position.y, position.z);
		}
	}
	
	@SuppressWarnings("serial")
	static final class AntLogMap extends HashMap<String, Object> {
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
