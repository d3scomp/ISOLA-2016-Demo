package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Arrays;
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
import cz.cuni.mff.d3s.deeco.knowledge.ValueSet;
import cz.cuni.mff.d3s.deeco.model.runtime.RuntimeModelHelper;
import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;
import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.runtime.DEECoRuntimeException;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;
import cz.cuni.mff.d3s.deeco.scheduler.Scheduler;
import cz.cuni.mff.d3s.deeco.task.TimerTask;
import cz.cuni.mff.d3s.deeco.task.TimerTaskListener;
import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;
import cz.cuni.mff.d3s.isola2016.demo.DemoLauncher;
import cz.cuni.mff.d3s.isola2016.demo.Mode;
import cz.cuni.mff.d3s.isola2016.demo.TimestampedFoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class IntelligentAntPlanning implements DEECoPlugin, TimerTaskListener {
	private DEECoContainer container;
	private AntAssignmetSolver solver;
	
	public IntelligentAntPlanning(AntAssignmetSolver solver) {
		this.solver = solver;
	}

	@Override
	public List<Class<? extends DEECoPlugin>> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public void init(DEECoContainer container) throws PluginInitFailedException {
		this.container = container;
		Scheduler scheduler = container.getRuntimeFramework().getScheduler();
		new TimerTask(scheduler, this, "FakeIntellignetEnsemble", 0, 10000).schedule();
	}

	@SuppressWarnings("unchecked")
	private AntInfo getKnowledge(ReadOnlyKnowledgeManager knowledgeManager) {
		try {
			KnowledgePath idPath = RuntimeModelHelper.createKnowledgePath("id");
			KnowledgePath positionPath = RuntimeModelHelper.createKnowledgePath("position");
			KnowledgePath foodsPath = RuntimeModelHelper.createKnowledgePath("foods");
			KnowledgePath modePath = RuntimeModelHelper.createKnowledgePath("mode");
			ValueSet set = knowledgeManager.get(Arrays.asList(idPath, foodsPath, positionPath, modePath));

			return new AntInfo((String) set.getValue(idPath), (Position) set.getValue(positionPath),
					(List<TimestampedFoodSource>) set.getValue(foodsPath), (Mode) set.getValue(modePath));

		} catch (KnowledgeNotFoundException e) {
			throw new DEECoRuntimeException("Knowledge extraction failed", e);
		}
	}

	private void setAssignedFoodSourceKnowledge(KnowledgeManager knowledgeManager, Position foodSourcePosition) {
		KnowledgePath knowledgePath = RuntimeModelHelper.createKnowledgePath("assignedFood");
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

		// Collect all foods
		Collection<FoodSource> foods = new LinkedHashSet<>();
		for (AntInfo ant : ants) {
			foods.addAll(ant.foods);
		}

		// Filter out pulling ants
		List<AntInfo> antsToRemove = new LinkedList<>();
		for (AntInfo ant : ants) {
			if (ant.mode != null && ant.mode == Mode.Pulling) {
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
		
		List<FoodSource> foodsToAdd = new LinkedList<>();
		// Multiply food sources according to capacity
		for(FoodSource source: foods) {
			for(int i = 0; i < source.portions;++i) {
				foodsToAdd.add(source);
			}
		}
		foods = foodsToAdd;

		Position assignedPosition = solver.solve(ants, foods, localAnt, DemoLauncher.ANT_HILL_POS);

		setAssignedFoodSourceKnowledge(container.getRuntimeFramework().getContainer().getLocals().iterator().next(),
				assignedPosition);
	}
}
