package cz.cuni.mff.d3s.isola2016.antsim;

import java.util.Collection;
import java.util.LinkedList;

import cz.cuni.mff.d3s.jdeeco.position.Position;

public class FoodPiece {
	public Position position;
	Collection<BigAntPlugin> pullers = new LinkedList<>();
	
	public FoodPiece(Position position, BigAntPlugin puller, Collection<BigAntPlugin> helpers) {
		this.position = position;
		this.pullers.add(puller);
		this.pullers.addAll(helpers);
	}
}
