package cz.cuni.mff.d3s.isola2016.demo;

import java.io.Serializable;

import cz.cuni.mff.d3s.jdeeco.position.Position;

@SuppressWarnings("serial")
public class FoodSource implements Serializable {
	public Position position;
	public Integer portions;
	public long timestamp;
		
	@Override
	public boolean equals(Object obj) {
		if(obj instanceof FoodSource) {
			FoodSource other = (FoodSource)obj;
			return position.equals(other.position);
		} else { 
			return false;
		}
	}
	
	@Override
	public int hashCode() {
		return position.hashCode();
	}
	
	public FoodSource(Position position, Integer portions, long timestamp) {
		this.position = position;
		this.portions = portions;
		this.timestamp = timestamp;
	}
	
	public FoodSource cloneWithTimestamp(long timestamp) {
		return new FoodSource(position, portions, timestamp);
	}
}
