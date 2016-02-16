package cz.cuni.mff.d3s.isola2016.demo;

import java.io.Serializable;

import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

@SuppressWarnings("serial")
public class TimestampedFoodSource extends FoodSource implements Serializable {
	public TimestampedFoodSource(Position position, Integer portions) {
		super(position, portions);
	}

	public TimestampedFoodSource(FoodSource source, long age) {
		super(source.position, source.portions);
		this.timestamp = age;
	}

	public long timestamp;
}
