package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeNotFoundException;
import cz.cuni.mff.d3s.deeco.knowledge.ReadOnlyKnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.ValueSet;
import cz.cuni.mff.d3s.deeco.model.runtime.RuntimeModelHelper;
import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;
import cz.cuni.mff.d3s.isola2016.demo.Mode;
import cz.cuni.mff.d3s.isola2016.demo.TimestampedFoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class AntInfo {
	public String id;
	public Position position;
	public List<TimestampedFoodSource> foods;
	public Mode mode;
	public Long time;
	
	@SuppressWarnings("unchecked")
	public AntInfo(ReadOnlyKnowledgeManager knowledgeManager) throws KnowledgeNotFoundException {
		KnowledgePath idPath = RuntimeModelHelper.createKnowledgePath("id");
		KnowledgePath positionPath = RuntimeModelHelper.createKnowledgePath("position");
		KnowledgePath foodsPath = RuntimeModelHelper.createKnowledgePath("foods");
		KnowledgePath modePath = RuntimeModelHelper.createKnowledgePath("mode");
		KnowledgePath timePath = RuntimeModelHelper.createKnowledgePath("time");
		ValueSet set = knowledgeManager.get(Arrays.asList(idPath, foodsPath, positionPath, modePath, timePath));

		this.id = (String) set.getValue(idPath);
		this.position  = (Position) set.getValue(positionPath);
		this.foods = (List<TimestampedFoodSource>) set.getValue(foodsPath);
		this.mode = (Mode) set.getValue(modePath);
		this.time = (Long) set.getValue(timePath);
	}

	public AntInfo(String id, Position position, List<TimestampedFoodSource> foods, Mode mode, Long time) {
		this.id = id;
		this.position = position;
		this.foods = foods;
		this.mode = mode;
		this.time = time;
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