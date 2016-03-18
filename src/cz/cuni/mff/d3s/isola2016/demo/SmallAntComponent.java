package cz.cuni.mff.d3s.isola2016.demo;

import java.util.Random;

import cz.cuni.mff.d3s.deeco.annotations.Component;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.Local;
import cz.cuni.mff.d3s.deeco.annotations.Out;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.annotations.Process;
import cz.cuni.mff.d3s.deeco.runtime.DEECoNode;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.deeco.timer.CurrentTimeProvider;
import cz.cuni.mff.d3s.isola2016.antsim.SmallAntPlugin;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.jdeeco.position.Position;

@Component
public class SmallAntComponent {
	public static final double RANDOM_WALK_DIAMETER = 15;
	public static final double RANDOM_WALK_DIAMETER_DIFF = 1;
	public String id;

	@Local
	public Position position;

	@Local
	public CurrentTimeProvider clock;

	@Local
	public SmallAntPlugin ant;

	@Local
	public Random rand;

	/// Initial knowledge
	public SmallAntComponent(int id, Random rand, DEECoNode node) {
		this.id = String.valueOf(id);
		this.rand = rand;
		this.clock = node.getRuntimeFramework().getScheduler().getTimer();
		this.ant = node.getPluginInstance(SmallAntPlugin.class);
	}

	/// Processes
	@Process
	@PeriodicScheduling(period = 500, order = 1)
	public static void sensePosition(@In("ant") SmallAntPlugin ant, @Out("position") ParamHolder<Position> position) {
		position.value = ant.getPosition();
	}

	@Process
	@PeriodicScheduling(period = 500, order = 7)
	public static void move(@In("ant") SmallAntPlugin ant, @In("rand") Random rand,
			@In("clock") CurrentTimeProvider clock) {
		if (ant.isAtTarget() || ant.getTarget() == null) {
			Position newPos = null;
			while(newPos == null || newPos.euclidDistanceTo(new Position(0, 0)) > RANDOM_WALK_DIAMETER) {
				newPos = PosUtils.getRandomPosition(rand, ant.getPosition().x, ant.getPosition().y, RANDOM_WALK_DIAMETER_DIFF);
			}
			ant.setTarget(newPos);
		}
	}
}
