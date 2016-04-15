package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import cz.cuni.mff.d3s.deeco.knowledge.ReadOnlyKnowledgeManager;
import cz.cuni.mff.d3s.deeco.knowledge.container.KnowledgeContainerException;
import cz.cuni.mff.d3s.deeco.knowledge.container.ReadOnlyKnowledgeWrapper;
import cz.cuni.mff.d3s.deeco.knowledge.container.TrackingKnowledgeContainer;
import cz.cuni.mff.d3s.deeco.knowledge.container.TrackingKnowledgeWrapper;
import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.runtime.DEECoPlugin;
import cz.cuni.mff.d3s.deeco.runtime.DEECoRuntimeException;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;
import cz.cuni.mff.d3s.deeco.runtime.RuntimeFramework;
import cz.cuni.mff.d3s.deeco.scheduler.Scheduler;
import cz.cuni.mff.d3s.deeco.task.TimerTask;
import cz.cuni.mff.d3s.deeco.task.TimerTaskListener;
import cz.cuni.mff.d3s.isola2016.demo.BigAnt;
import cz.cuni.mff.d3s.isola2016.demo.DemoLauncher;
import cz.cuni.mff.d3s.isola2016.demo.FoodSource;

public class IntelligentAntPlanning implements DEECoPlugin, TimerTaskListener {
	private RuntimeFramework runtime;
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
		this.runtime = container.getRuntimeFramework();
		Scheduler scheduler = container.getRuntimeFramework().getScheduler();
		new TimerTask(scheduler, this, "FakeIntellignetEnsemble", 0, 1000).schedule();
	}

	@Override
	public void at(long time, Object triger) {
		try {
			evaluateEnsemble();
		} catch (KnowledgeContainerException e) {
			throw new DEECoRuntimeException("Intelligent ensemble evaluation failed", e);
		}
	}

	/**
	 * Collect all foods, prefer up-to-date information
	 */
	private Collection<FoodSource> extractSources(Collection<BigAnt> ants) {
		Set<FoodSource> foods = new LinkedHashSet<>();
		for (BigAnt ant : ants) {
			for (FoodSource source : ant.foods) {
				if (!foods.contains(source)) {
					foods.add(source);
				} else {
					// Contained, add only when newer
					for (FoodSource s : foods) {
						if (s.equals(source) && source.timestamp > s.timestamp) {
							foods.add(source);
							break;
						}
					}
				}
			}
		}
		return foods;
	}

	/**
	 * This simulates evaluation of intelligent ensemble
	 * 
	 * @throws KnowledgeContainerException
	 */
	private void evaluateEnsemble() throws KnowledgeContainerException {
		// Setup knowledge manager wrappers
		TrackingKnowledgeWrapper localWrapper = new TrackingKnowledgeWrapper(
				runtime.getContainer().getLocals().iterator().next());
		Collection<ReadOnlyKnowledgeWrapper> remoteWrappers = new LinkedHashSet<>();
		for (ReadOnlyKnowledgeManager remote : runtime.getContainer().getReplicas()) {
			remoteWrappers.add(new ReadOnlyKnowledgeWrapper(remote));
		}
		TrackingKnowledgeContainer trackingContainer = new TrackingKnowledgeContainer(localWrapper, remoteWrappers);

		Collection<BigAnt> ants = trackingContainer.getTrackedKnowledgeForRole(BigAnt.class);
		
		// Filter-out too old ant data
		for(Iterator<BigAnt> it = ants.iterator(); it.hasNext();) {
			BigAnt ant = it.next();
			if(runtime.getScheduler().getTimer().getCurrentMilliseconds() - ant.time > maxTimeSkew) {
				it.remove();
			}
			
		}
		
		Collection<FoodSource> sources = extractSources(ants);
		
		// Solve the problem
		solver.solve(ants, sources, DemoLauncher.ANT_HILL_POS);
		
		trackingContainer.commitChanges();
	}
}
