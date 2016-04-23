package cz.cuni.mff.d3s.isola2016.demo;

import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.Role;
import cz.cuni.mff.d3s.jdeeco.position.Position;

@Role
public class BigAnt {
	public String id;
	public Position position;
	public List<FoodSource> foods;
	public Mode mode;
	public Long time;
	
	public Position assignedFood;
	public Long assignedFoodTime;
	public Double accumulatedFitness;
	
	public Double currentGoalUtility;
	public Double totalUtility;
}