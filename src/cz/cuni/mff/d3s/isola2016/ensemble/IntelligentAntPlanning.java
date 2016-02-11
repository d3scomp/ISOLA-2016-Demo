package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;

import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeNotFoundException;
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
			ValueSet set = knowledgeManager.get(Arrays.asList(idPath, foodsPath, positionPath));
			
			return new AntInfo((String) set.getValue(idPath), (Position) set.getValue(positionPath),
					(List<FoodSourceEx>) set.getValue(foodsPath));

		} catch (KnowledgeNotFoundException e) {
			throw new DEECoRuntimeException("Knowledge extraction failed", e);
		}
	}
	
	private double getBadnessAntAntFood(AntInfo a, AntInfo b, FoodSource f) {
		return a.position.euclidDistanceTo(f.position) + b.position.euclidDistanceTo(f.position);
	}

	@Override
	public void at(long time, Object triger) {
		System.out.println("Fake intelligence being performed");
		
		// Get local knowledge
		AntInfo localAnt = getKnowledge(container.getRuntimeFramework().getContainer().getLocals().iterator().next());
			
		// Get remote knowledge
		Collection<AntInfo> remoteAnts = new HashSet<>();
		for(ReadOnlyKnowledgeManager remote: container.getRuntimeFramework().getContainer().getReplicas()) {
			remoteAnts.add(getKnowledge(remote));
		}
		
		System.out.println("Local: ");
		System.out.println(localAnt);
		System.out.println("Remote:");
		for(AntInfo info: remoteAnts) {
			System.out.println(info);
		}
		
		// Collect all ants
		Collection<AntInfo> ants = new HashSet<>();
		ants.add(localAnt);
		ants.addAll(remoteAnts);
		
		// Collect all foods
		Collection<FoodSource> foods = new HashSet<>();
		for(AntInfo ant: ants) {
			foods.addAll(ant.foods);
		}
		
		Collection<Collection<Triplet>> combined = Combiner.combine(ants, foods);
		
	}
}
