package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
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
import cz.cuni.mff.d3s.isola2016.demo.AntComponent.FoodSourceEx;
import cz.cuni.mff.d3s.isola2016.demo.AntComponent.Mode;
import cz.cuni.mff.d3s.isola2016.ensemble.Combiner.Triplet;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class IntelligentAntPlanning implements DEECoPlugin, TimerTaskListener {
	private DEECoContainer container;

	@Override
	public List<Class<? extends DEECoPlugin>> getDependencies() {
		return Collections.emptyList();
	}

	@Override
	public void init(DEECoContainer container) throws PluginInitFailedException {
		this.container = container;
		Scheduler scheduler = container.getRuntimeFramework().getScheduler();
		new TimerTask(scheduler, this, "FakeIntellignetEnsemble", 0, 3000).schedule();
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
					(List<FoodSourceEx>) set.getValue(foodsPath), (Mode) set.getValue(modePath));

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

	private double getBadnessAntAntFood(AntInfo a, AntInfo b, FoodSource f) {
		return a.position.euclidDistanceTo(f.position) + b.position.euclidDistanceTo(f.position);
	}

	private double getSolutionBadness(Collection<Triplet> triplets) {
		double badness = 0;
		for (Triplet t : triplets) {
			badness += getBadnessAntAntFood(t.a, t.b, t.c);
		}
		return badness;
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
		Collection<AntInfo> remoteAnts = new HashSet<>();
		for (ReadOnlyKnowledgeManager remote : container.getRuntimeFramework().getContainer().getReplicas()) {
			remoteAnts.add(getKnowledge(remote));
		}
		/*
		 * System.out.println("Local: "); System.out.println(localAnt); System.out.println("Remote:"); for(AntInfo info:
		 * remoteAnts) { System.out.println(info); }
		 */

		// Collect all ants
		Collection<AntInfo> ants = new HashSet<>();
		ants.add(localAnt);
		ants.addAll(remoteAnts);

		// Collect all foods
		Collection<FoodSource> foods = new HashSet<>();
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

		// Generate all possible solutions
		Collection<Collection<Triplet>> combined = Combiner.combine(ants, foods);

		System.out.print("Fake intelligence: Ants: " + ants.size() + " foods: " + foods.size() + " => "
				+ combined.size() + " alternatives --- ");
		
		// Get best solution
		double bestBadness = Double.POSITIVE_INFINITY;
		Collection<Triplet> bestSolution = null;
		for (Collection<Triplet> solution : combined) {
			double badness = getSolutionBadness(solution);
			if (badness <= bestBadness) {
				bestBadness = badness;
				bestSolution = solution;
			}
		}

		// Get food assigned to local ant
		FoodSource sourceAssignedToLocalAnt = null;
		for (Triplet t : bestSolution) {
			if (t.a == localAnt || t.b == localAnt) {
				sourceAssignedToLocalAnt = t.c;
			}
		}

		System.out.print("Ant " + localAnt.id + " assigned to food: ");
		if (sourceAssignedToLocalAnt != null) {
			System.out.println(sourceAssignedToLocalAnt.position);
		} else {
			System.out.println("null");
		}

		// Set assigned food source local knowledge
		Position assignedPosition = null;
		if (sourceAssignedToLocalAnt != null) {
			assignedPosition = sourceAssignedToLocalAnt.position;
		}

		setAssignedFoodSourceKnowledge(container.getRuntimeFramework().getContainer().getLocals().iterator().next(),
				assignedPosition);
	}
}
