package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.List;

import cz.cuni.mff.d3s.isola2016.demo.AntComponent.FoodSourceEx;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class AntInfo {
	public String id;
	public Position position;
	public List<FoodSourceEx> foods;

	public AntInfo(String id, Position position, List<FoodSourceEx> foods) {
		this.id = id;
		this.position = position;
		this.foods = foods;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("id: %s, Pos: %s, Foods: [", id, position!=null?position.toString():"null"));
		boolean first = true;
		for (FoodSourceEx food : foods) {
			if (first) {
				first = false;
			} else {
				builder.append(", ");
			}
			builder.append(food.position.euclidDistanceTo(position));
		}
		builder.append("]");

		return builder.toString();
	}
}