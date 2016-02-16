package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.List;

import cz.cuni.mff.d3s.isola2016.demo.Mode;
import cz.cuni.mff.d3s.isola2016.demo.TimestampedFoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class AntInfo {
	public String id;
	public Position position;
	public List<TimestampedFoodSource> foods;
	public Mode mode;

	public AntInfo(String id, Position position, List<TimestampedFoodSource> foods, Mode mode) {
		this.id = id;
		this.position = position;
		this.foods = foods;
		this.mode = mode;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("id: %s, Pos: %s, Foods: [", id, position!=null?position.toString():"null"));
		boolean first = true;
		for (TimestampedFoodSource food : foods) {
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