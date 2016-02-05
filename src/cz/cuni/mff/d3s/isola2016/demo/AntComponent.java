package cz.cuni.mff.d3s.isola2016.demo;

import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.jdeeco.position.Position;

@Component
public class AntComponent {
	/// Knowledge
	static class Food {
		Position poition;
		Integer portions;
	}
	public String id;
	public Position position;
	public List<Food> food;
	
	/// Initial knowledge
	public AntComponent(int id) {
		this.id = String.valueOf(id);
	}
	
	/// Processes
	@Process
	@PeriodicScheduling(period = 5000)
	public static void printStatus() {
		System.out.format("Ant status%n");
	}
	
	@Process
	@PeriodicScheduling(period = 1000)
	public static void move() {
		
	}
}
