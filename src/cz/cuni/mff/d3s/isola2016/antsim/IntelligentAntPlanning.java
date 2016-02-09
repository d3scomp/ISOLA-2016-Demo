package cz.cuni.mff.d3s.isola2016.antsim;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.KnowledgeNotFoundException;
import cz.cuni.mff.d3s.deeco.knowledge.ValueSet;
import cz.cuni.mff.d3s.deeco.model.runtime.RuntimeModelHelper;
import cz.cuni.mff.d3s.deeco.model.runtime.api.KnowledgePath;
import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;
import cz.cuni.mff.d3s.deeco.scheduler.Scheduler;
import cz.cuni.mff.d3s.deeco.task.TimerTask;
import cz.cuni.mff.d3s.deeco.task.TimerTaskListener;
import cz.cuni.mff.d3s.isola2016.demo.AntComponent.FoodSourceEx;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class IntelligentAntPlanning implements DEECoPlugin, TimerTaskListener {
	private static class AntInfo {
		public String id;
		public Position position;
		public List<FoodSourceEx> foods;
		
		public AntInfo(String id, Position position, List<FoodSourceEx> foods) {
			 this.id = id;
			 this.position = position;
			 this.foods = foods;
		}
		
		@Override
		public String toString() {
			return String.format("id: %s, Pos: %s, Foods: %s", id, position.toString(), foods);
		}
	}
	
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

	@Override
	public void at(long time, Object triger) {
		System.out.println("Fake intelligence being performed");
		
		for(KnowledgeManager local: container.getRuntimeFramework().getContainer().getLocals()) {
			try {
				KnowledgePath idPath = RuntimeModelHelper.createKnowledgePath("id");
				KnowledgePath positionPath = RuntimeModelHelper.createKnowledgePath("position");
				KnowledgePath foodsPath = RuntimeModelHelper.createKnowledgePath("foods");
				ValueSet set = local.get(Arrays.asList(
						idPath, foodsPath, positionPath
				));
				
				AntInfo ant = new AntInfo((String)set.getValue(idPath), (Position)set.getValue(positionPath), (List<FoodSourceEx>)set.getValue(foodsPath));
				
				System.out.println(ant);
				
			} catch (KnowledgeNotFoundException e) {
				e.printStackTrace();
			}
		}
	}
}
