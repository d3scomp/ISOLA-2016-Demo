package cz.cuni.mff.d3s.isola2016.antsim;

import cz.cuni.mff.d3s.jdeeco.position.Position;

public class FoodSource {
	public Position position;
	public Integer portions;
	
	public FoodSource(Position position, Integer portions) {
		this.position = position;
		this.portions = portions;
	}
}
