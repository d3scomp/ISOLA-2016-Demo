package cz.cuni.mff.d3s.isola2016.demo;

import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Local;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.runners.DEECoSimulation;
import cz.cuni.mff.d3s.deeco.timer.CurrentTimeProvider;
import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;
import cz.cuni.mff.d3s.jdeeco.network.omnet.OMNeTSimulation.Timer;
import cz.cuni.mff.d3s.jdeeco.position.Position;

@Component
public class AntComponent {
	public String id;
	public Position position;
	public List<FoodSource> food;
	
	@Local
	public CurrentTimeProvider clock;
	
	/// Initial knowledge
	public AntComponent(int id, Timer timer, DEECoSimulation realm) {
		this.id = String.valueOf(id);
		this.clock = timer;
		// TODO: Get ant simulation plugin instance 
	}
	
	/// Processes
	@Process
	@PeriodicScheduling(period = 5000)
	public static void printStatus(@In("clock") CurrentTimeProvider clock, @In("id") String id) {
		System.out.format("%06d: Ant %s, status%n", clock.getCurrentMilliseconds(), id);
	}
	
	@Process
	@PeriodicScheduling(period = 1000)
	public static void move() {
		
	}
}
