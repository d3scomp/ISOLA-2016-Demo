package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;

import cz.cuni.mff.d3s.isola2016.demo.BigAnt;
import cz.cuni.mff.d3s.isola2016.demo.Config;
import cz.cuni.mff.d3s.isola2016.demo.FoodSource;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class HeuristicSolver implements AntAssignmetSolver {
	public static final double MAX_DISTANCE_M = 30;
	public final Config config;

	class Ensemble {
		public Set<BigAnt> ants = new LinkedHashSet<>();
		public FoodSource source;
		private Double appFitnessCache;
		private Double latFitnessCache;
		final long curTime;

		public Ensemble(Collection<BigAnt> ants, FoodSource source, long curTime) {
			if (ants.size() != 2) {
				throw new UnsupportedOperationException("Num of ants in ensemble must be 2, but is " + ants.size());
			}

			this.ants.addAll(ants);
			this.source = source;
			this.curTime = curTime;
		}

		public Ensemble(BigAnt antA, BigAnt antB, FoodSource source, long curTime) {
			this(Arrays.asList(antA, antB), source, curTime);
		}

		public void commit() {
			for (BigAnt ant : ants) {
				ant.assignedFood = source.position;
				ant.currentGoalUtility = getAppFitness();
			}
		}

		private double getAppFitness() {
			if (appFitnessCache == null) {
				double totalDistance = ants.stream().mapToDouble(ant -> ant.position.euclidDistanceTo(source.position))
						.average().getAsDouble();

				switch (mode) {
				case PreferCloseFoods:
					appFitnessCache = 1 - Math.min(1, totalDistance / MAX_DISTANCE_M);
					break;
				case PreferDistantFoods:
					appFitnessCache = Math.min(1, totalDistance / MAX_DISTANCE_M);
					break;
				case PreferNeutral:
					appFitnessCache = new Random(source.position.hashCode()).nextDouble();
					break;
				default:
					throw new UnsupportedOperationException("Fitness calculation not defined for mode: " + mode);
				}
			}
			if (appFitnessCache < 0.5) {
				appFitnessCache = 0.0;
			}
			return appFitnessCache;
		}

		private double getLatFitness() {
			if (latFitnessCache == null) {
				double totalLatency = ants.stream().map(ant -> ant.time).reduce(0l,
						(sum, time) -> sum += curTime - time);
				latFitnessCache = 1 - Math.min(1, totalLatency / config.maxTimeSkewMs);
			}
			return latFitnessCache;
		}
	}

	class PersistentEnsemble {
		Collection<String> antIds;
		Position sourcePosition;

		List<Ensemble> instances = new LinkedList<>();

		public PersistentEnsemble(Ensemble ensemble) {
			antIds = ensemble.ants.stream().map(ant -> ant.id).collect(Collectors.toSet());
			sourcePosition = ensemble.source.position;
			instances.add(ensemble);
		}

		public Ensemble tryMaintain(Collection<BigAnt> ants, List<FoodSource> foods, long curTime) {
			Set<BigAnt> matchedAnts = new LinkedHashSet<>();
			FoodSource matchedSource = null;

			// Match ants
			assert (!antIds.isEmpty());
			Iterator<String> antIdIt = antIds.iterator();
			String curId = antIdIt.next();
			for (BigAnt ant : ants) {
				if (ant.id.equals(curId)) {
					matchedAnts.add(ant);
					if (antIdIt.hasNext()) {
						curId = antIdIt.next();
					} else {
						break;
					}
				}
			}

			// Match source
			for (FoodSource source : foods) {
				if (PosUtils.isSame(source.position, sourcePosition)) {
					matchedSource = source;
					break;
				}
			}

			// Ensemble cannot be maintained as we do not have ants and food to maintain it
			if (matchedAnts.size() != antIds.size() || matchedSource == null) {
				return null;
			}

			Ensemble ensemble = new Ensemble(matchedAnts, matchedSource, curTime);

			// Ensemble cannot be maintained, fitness condition prevents it
			instances.add(ensemble);

			if (!checkPerisitanceCondition()) {
				return null;
			}

			return ensemble;
		}

		public boolean checkPerisitanceCondition() {
			double avgLatFitness = instances.stream().mapToDouble(ens -> ens.getLatFitness()).average().getAsDouble();
			return avgLatFitness > 0.5;
		}
	}

	final FitnessMode mode;
	final Set<PersistentEnsemble> persistentEnsembles = new LinkedHashSet<>();

	public HeuristicSolver(FitnessMode mode, Config config) {
		this.mode = mode;
		this.config = config;
	}

	private Collection<Ensemble> generateOptions(List<BigAnt> ants, List<FoodSource> foods, long curTime) {
		Set<Ensemble> options = new LinkedHashSet<>();

		// Generate all triplet with unique unsorted ant pairs
		for (ListIterator<BigAnt> antAIt = ants.listIterator(); antAIt.hasNext();) {
			BigAnt antA = antAIt.next();
			for (ListIterator<BigAnt> antBIt = ants.listIterator(antAIt.nextIndex()); antBIt.hasNext();) {
				BigAnt antB = antBIt.next();
				for (FoodSource source : foods) {
					options.add(new Ensemble(antA, antB, source, curTime));
				}
			}
		}

		return options;
	}

	@Override
	public void solve(Collection<BigAnt> ants, Collection<FoodSource> foods, Position antHill, long curTime) {
		List<BigAnt> remainingAnts = new LinkedList<>(ants);
		List<FoodSource> remainingFoods = new LinkedList<>(foods);

		// Try to maintain persistent ensembles
		for (Iterator<PersistentEnsemble> it = persistentEnsembles.iterator(); it.hasNext();) {
			PersistentEnsemble persistentEnsemble = it.next();
			Ensemble ensemble = persistentEnsemble.tryMaintain(remainingAnts, remainingFoods, curTime);
			if (ensemble == null) {
				// Maintenance failed, drop persistent ensemble
				it.remove();
			} else {
				// Prolong ensemble
				ensemble.commit();
				remainingAnts.removeAll(ensemble.ants);
				remainingFoods.remove(ensemble.source);
			}
		}

		// Create new ensembles
		while (!remainingAnts.isEmpty() && !remainingFoods.isEmpty()) {
			// Generate all options, break when no options
			Collection<Ensemble> options = generateOptions(remainingAnts, remainingFoods, curTime);
			if (options.isEmpty()) {
				break;
			}

			// Find best option
			Ensemble best = null;
			for (Ensemble ensemble : options) {
				if (best == null || (ensemble.getAppFitness() > 0 && ensemble.getAppFitness() > best.getAppFitness())) {
					best = ensemble;
				}
			}

			// Do "knowledge exchange" for best
			best.commit();
			persistentEnsembles.add(new PersistentEnsemble(best));

			System.err.println(best.getAppFitness());

			// Remove best from remaining
			remainingAnts.removeAll(best.ants);
			remainingFoods.remove(best.source);
		}

		// Set no food position to remaining ants
		for (BigAnt ant : remainingAnts) {
			ant.assignedFood = null;
			ant.currentGoalUtility = 0.0;
		}
	}
}
