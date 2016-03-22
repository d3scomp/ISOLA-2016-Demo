package cz.cuni.mff.d3s.isola2016.ensemble;

import java.io.Serializable;
import java.util.Arrays;
import java.util.List;

import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeNotFoundException;
import cz.cuni.mff.d3s.deeco.knowledge.ReadOnlyKnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.ValueSet;
import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;
import cz.cuni.mff.d3s.deeco.model.runtime.custom.KnowledgePathExt;
import cz.cuni.mff.d3s.isola2016.demo.Mode;
import cz.cuni.mff.d3s.isola2016.demo.TimestampedFoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

@SuppressWarnings("serial")
public class AntInfo implements Serializable {
	public String id;
	public Position position;
	public List<TimestampedFoodSource> foods;
	public Mode mode;
	public Long time;
	public Position assistantPos;
	public Position assignedFood;
	
	@SuppressWarnings("unchecked")
	public AntInfo(ReadOnlyKnowledgeManager knowledgeManager) throws KnowledgeNotFoundException {
		KnowledgePath idPath = KnowledgePathExt.createKnowledgePath("id");
		KnowledgePath positionPath = KnowledgePathExt.createKnowledgePath("position");
		KnowledgePath foodsPath = KnowledgePathExt.createKnowledgePath("foods");
		KnowledgePath modePath = KnowledgePathExt.createKnowledgePath("mode");
		KnowledgePath timePath = KnowledgePathExt.createKnowledgePath("time");
		KnowledgePath assistantPath = KnowledgePathExt.createKnowledgePath("assistantPos");
		KnowledgePath assistantFoodPath = KnowledgePathExt.createKnowledgePath("assignedFood");
		ValueSet set = knowledgeManager.get(Arrays.asList(idPath, foodsPath, positionPath, modePath, timePath, assistantPath, assistantFoodPath));

		this.id = (String) set.getValue(idPath);
		this.position  = (Position) set.getValue(positionPath);
		this.foods = (List<TimestampedFoodSource>) set.getValue(foodsPath);
		this.mode = (Mode) set.getValue(modePath);
		this.time = (Long) set.getValue(timePath);
		this.assistantPos = (Position) set.getValue(assistantPath);
		this.assignedFood = (Position) set.getValue(assistantFoodPath);
	}

	public AntInfo(String id, Position position, List<TimestampedFoodSource> foods, Mode mode, Long time, Position assistantPos, Position assignedFood) {
		this.id = id;
		this.position = position;
		this.foods = foods;
		this.mode = mode;
		this.time = time;
		this.assistantPos = assistantPos;
		this.assignedFood = assignedFood;
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