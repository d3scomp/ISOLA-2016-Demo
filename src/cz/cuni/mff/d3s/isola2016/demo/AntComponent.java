package cz.cuni.mff.d3s.isola2016.demo;

import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class AntComponent {
	/// Knowledge
	static class Food {
		Position poition;
		Integer portions;
	}
	String id;
	Position position;
	List<Food> food;
	
	/// Initial knowledge
	public AntComponent() {
		
	}
	
	/// Processes
	@Process
	@PeriodicScheduling(period = 5000)
	static void printStatus() {
		System.out.format("Ant status%n");
	}
	
	@Process
	@PeriodicScheduling(period = 1000)
	static void move() {
		
	}
}
