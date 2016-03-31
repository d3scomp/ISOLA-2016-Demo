package cz.cuni.mff.d3s.isola2016.utils;

import java.util.HashMap;
import java.util.Map;

import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogRecord;
import cz.cuni.mff.d3s.isola2016.antsim.AbstractAntWorldPlugin;

public class AntWorldStateRecord extends RuntimeLogRecord {
	public static AntWorldStateRecord create(AbstractAntWorldPlugin world) {
		Map<String, Object> values = new HashMap<>();
		
		
		
		return new AntWorldStateRecord(values);
	}
	
	public AntWorldStateRecord(Map<String, Object> values) {
		super("world", values);
	}

}
