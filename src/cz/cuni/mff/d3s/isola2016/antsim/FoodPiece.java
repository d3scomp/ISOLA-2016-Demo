package cz.cuni.mff.d3s.isola2016.antsim;

import java.util.Collection;
import java.util.LinkedList;

import cz.cuni.mff.d3s.jdeeco.position.Position;

public class FoodPiece {
	public Position position;
	Collection<AntPlugin> pullers = new LinkedList<>();
	
	public FoodPiece(Position position, AntPlugin pullerA, AntPlugin pullerB) {
		this.position = position;
		this.pullers.add(pullerA);
		this.pullers.add(pullerB);
	}
}
