package cz.cuni.mff.d3s.isola2016.demo;

import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Local;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.timer.CurrentTimeProvider;
import cz.cuni.mff.d3s.isola2016.antsim.AntPlugin;
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

	@Local
	public AntPlugin ant;

	/// Initial knowledge
	public AntComponent(int id, Timer timer, AntPlugin ant) {
		this.id = String.valueOf(id);
		this.clock = timer;
		this.ant = ant;
	}

	/// Processes
	@Process
	@PeriodicScheduling(period = 5000)
	public static void printStatus(@In("clock") CurrentTimeProvider clock, @In("id") String id,
			@In("ant") AntPlugin ant) {
		System.out.format("%06d: Ant %s, %s, %s%n", clock.getCurrentMilliseconds(), id, ant.getPosition(), ant.getState());
	}

	@Process
	@PeriodicScheduling(period = 1000)
	public static void move(@In("ant") AntPlugin ant) {
		ant.setTarget(new Position(3, 4));
	}
}
