package cz.cuni.mff.d3s.isola2016.antsim;

import cz.cuni.mff.d3s.jdeeco.position.Position;

public class FoodSource {
	public Position position;
	public Integer portions;
	
	public FoodSource(Position position, Integer portions) {
		this.position = position;
		this.portions = portions;
	}
	
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
}
