package cz.cuni.mff.d3s.isola2016.utils;

import java.io.IOException;
import java.util.Collection;
import java.util.HashMap;

import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogRecord;
import cz.cuni.mff.d3s.deeco.runtimelog.RuntimeLogger;
import cz.cuni.mff.d3s.isola2016.demo.FoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public final class FoodLogRecord extends RuntimeLogRecord {
	@SuppressWarnings("serial")
	private static final class FoodLogMap extends HashMap<String, Object> {
		public FoodLogMap(Position position) {
			this.put("positon", new XMLPosition(position));
		}
	}

	public XMLPosition xmlPos;

	public FoodLogRecord(FoodSource source) {
		super("World", new FoodLogMap(source.position));
		xmlPos = new XMLPosition(source.position);
	}
	
	public static void logAll(RuntimeLogger logger, Collection<? extends FoodSource> sources) throws IllegalStateException, IOException {
		for(FoodSource source: sources) {
			logger.log(new FoodLogRecord(source));
		}
	}
}
