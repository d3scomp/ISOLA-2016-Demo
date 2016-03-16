package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;

import cz.cuni.mff.d3s.deeco.knowledge.ChangeSet;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeNotFoundException;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeUpdateException;
import cz.cuni.mff.d3s.deeco.knowledge.ReadOnlyKnowledgeManager;
import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;
import cz.cuni.mff.d3s.deeco.model.runtime.custom.KnowledgePathExt;
import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.runtime.DEECoRuntimeException;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;
import cz.cuni.mff.d3s.deeco.scheduler.Scheduler;
import cz.cuni.mff.d3s.deeco.task.TimerTask;
import cz.cuni.mff.d3s.deeco.task.TimerTaskListener;
import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;
import cz.cuni.mff.d3s.isola2016.demo.DemoLauncher;
import cz.cuni.mff.d3s.isola2016.demo.TimestampedFoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class IntelligentAntPlanning implements DEECoPlugin, TimerTaskListener {
	private DEECoContainer container;
	private final AntAssignmetSolver solver;
	private final long maxTimeSkew; 
	
	public IntelligentAntPlanning(AntAssignmetSolver solver, long maxTimeSkew) {
		this.solver = solver;
		this.maxTimeSkew = maxTimeSkew;
	}

	@Override
	public List<Class<? extends DEECoPlugin>> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public void init(DEECoContainer container) throws PluginInitFailedException {
		this.container = container;
		Scheduler scheduler = container.getRuntimeFramework().getScheduler();
		new TimerTask(scheduler, this, "FakeIntellignetEnsemble", 0, 1000).schedule();
	}

	private AntInfo getKnowledge(ReadOnlyKnowledgeManager knowledgeManager) {
		try {
			return new AntInfo(knowledgeManager);
		} catch (KnowledgeNotFoundException e) {
			throw new DEECoRuntimeException("Knowledge extraction failed", e);
		}
	}

	private void setAssignedFoodSourceKnowledge(KnowledgeManager knowledgeManager, Position foodSourcePosition) {
		KnowledgePath knowledgePath = KnowledgePathExt.createKnowledgePath("assignedFood");
		try {
			ChangeSet changes = new ChangeSet();
			changes.setValue(knowledgePath, foodSourcePosition);
			knowledgeManager.update(changes);

		} catch (KnowledgeUpdateException e) {
			throw new DEECoRuntimeException("Knowledge insertion failed", e);
		}
	}

	@Override
	public void at(long time, Object triger) {
		evaluateEnsemble();
	}

	/**
	 * This simulates evaluation of intelligent ensemble
	 */
	private void evaluateEnsemble() {
		// Get local knowledge
		AntInfo localAnt = getKnowledge(container.getRuntimeFramework().getContainer().getLocals().iterator().next());

		// Get remote knowledge
		Collection<AntInfo> remoteAnts = new LinkedHashSet<>();
		for (ReadOnlyKnowledgeManager remote : container.getRuntimeFramework().getContainer().getReplicas()) {
			remoteAnts.add(getKnowledge(remote));
		}
		/*
		 * System.out.println("Local: "); System.out.println(localAnt); System.out.println("Remote:"); for(AntInfo info:
		 * remoteAnts) { System.out.println(info); }
		 */

		// Collect all ants
		Collection<AntInfo> ants = new LinkedHashSet<>();
		ants.add(localAnt);
		ants.addAll(remoteAnts);

		// Collect all foods, prefer up-to-date information
		Collection<TimestampedFoodSource> foods = new LinkedHashSet<>();
		for (AntInfo ant : ants) {
			for(TimestampedFoodSource source: ant.foods) {
				if(!foods.contains(source)) {
					long off = container.getRuntimeFramework().getScheduler().getTimer().getCurrentMilliseconds() - source.timestamp;
					if(off < maxTimeSkew) {
						// Not contained add
						foods.add(source);
					}
				} else {
					// Contained, add only when newer
					boolean add = false;
					for(TimestampedFoodSource s: foods) {
						if(s.equals(source)) {
							if(source.timestamp > s.timestamp) {
								add = true;
							}
						}
					}
					if(add) {
						foods.add(source);
					}
				}
			}
			foods.addAll(ant.foods);
		}
		
		// Filter out old ants data
		List<AntInfo> oldAntsToRemove = new LinkedList<>();
		for (AntInfo ant : ants) {
			if (ant.time == null) {
				System.err.println("Removing ant infor, time == null");
				oldAntsToRemove.add(ant);
			} else {
				long off = container.getRuntimeFramework().getScheduler().getTimer().getCurrentMilliseconds() - ant.time;
				if(off > maxTimeSkew) {
					System.err.println("Removing ant info, time is off by: " + off);
					oldAntsToRemove.add(ant);
				}
			}
		}
		ants.removeAll(oldAntsToRemove);
		

		// Filter out pulling ants
		List<AntInfo> antsToRemove = new LinkedList<>();
		for (AntInfo ant : ants) {
			if (ant.mode != null && !ant.mode.isPlanable()) {
				antsToRemove.add(ant);
			}
		}
		ants.removeAll(antsToRemove);

		// Filter out empty food sources
		List<FoodSource> foodsToRemove = new LinkedList<>();
		for (FoodSource source : foods) {
			if (source.portions == 0) {
				foodsToRemove.add(source);
			}
		}
		foods.removeAll(foodsToRemove);
		
		List<FoodSource> outFoods = new LinkedList<>();
		// Multiply food sources according to capacity
		for(FoodSource source: foods) {
			for(int i = 0; i < source.portions;++i) {
				outFoods.add(source);
			}
		}
		
		Position assignedPosition = solver.solve(ants, outFoods, localAnt, DemoLauncher.ANT_HILL_POS);

		setAssignedFoodSourceKnowledge(container.getRuntimeFramework().getContainer().getLocals().iterator().next(),
				assignedPosition);
	}
}
