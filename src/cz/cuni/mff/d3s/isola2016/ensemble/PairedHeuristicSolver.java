package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Random;
import java.util.Set;

import cz.cuni.mff.d3s.isola2016.antsim.QuantumFoodSource;
import cz.cuni.mff.d3s.isola2016.demo.BigAnt;
import cz.cuni.mff.d3s.isola2016.demo.Config;
import cz.cuni.mff.d3s.isola2016.demo.FoodSource;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class PairedHeuristicSolver implements AntAssignmetSolver {
	public static final double MAX_DISTANCE_M = 30;
	public final Config config;

	class Ensemble {
		public Map<BigAnt, QuantumFoodSource> antToSource = new LinkedHashMap<>();
		private Double appFitnessCache;
		private Double latFitnessCache;
		private Double sourceFitnessCache;
		final long curTime;

		public Ensemble(List<BigAnt> ants, List<QuantumFoodSource> sources, long curTime) {
			if (ants.size() != 2) {
				throw new UnsupportedOperationException("Num of ants in ensemble must be 2, but is " + ants.size());
			}

			if (sources.stream().map(source -> source.quantumId).distinct().count() != 1) {
				throw new UnsupportedOperationException(
						"Attempt to create ensemble with food not matching quantum ids");
			}

			if (sources.size() != ants.size()) {
				throw new UnsupportedOperationException("Attempt to create ensemble num ants != num sources");
			}

			for (int i = 0; i < ants.size(); ++i) {
				antToSource.put(ants.get(i), sources.get(i));
			}

			this.curTime = curTime;
		}

		public Ensemble(BigAnt antA, BigAnt antB, QuantumFoodSource sourceA, QuantumFoodSource sourceB, long curTime) {
			this(Arrays.asList(antA, antB), Arrays.asList(sourceA, sourceB), curTime);
		}

		public void commit() {
			for (Entry<BigAnt, QuantumFoodSource> entry : antToSource.entrySet()) {
				entry.getKey().assignedFood = entry.getValue().position;
				entry.getKey().currentGoalUtility = getSourceFitness();
			}
		}

		public double getAppFitness() {
			if (appFitnessCache == null) {
				appFitnessCache = getAntFitness() + getSourceFitness();
			}
			return appFitnessCache;
		}

		private double getAntFitness() {
			double totalDistance = antToSource.entrySet().stream()
					.mapToDouble(entry -> entry.getKey().position.euclidDistanceTo(entry.getValue().position)).average()
					.getAsDouble();

			return 1 - Math.min(1, totalDistance / MAX_DISTANCE_M);
		}

		private double getSourceFitness() {
			if (sourceFitnessCache == null) {
				final Position center = new Position(
						getSources().stream().mapToDouble(src -> src.position.x).average().getAsDouble(),
						getSources().stream().mapToDouble(src -> src.position.y).average().getAsDouble());
				final double totalDistance = getSources().stream()
						.mapToDouble(src -> src.position.euclidDistanceTo(center)).sum();

				switch (mode) {
				case PreferCloseFoods:
					sourceFitnessCache = Math.pow(1 - Math.min(1, totalDistance / MAX_DISTANCE_M), 0.5);
					break;
				case PreferDistantFoods:
					sourceFitnessCache = 100 * Math.pow(Math.min(1, totalDistance / MAX_DISTANCE_M), 0.5);
					break;
				case PreferNeutral:
					sourceFitnessCache = new Random(getSources().iterator().next().quantumId).nextDouble();
					break;
				default:
					throw new UnsupportedOperationException("Fitness calculation not defined for mode: " + mode);
				}
			}
			return sourceFitnessCache;
		}

		public double getLatFitness() {
			if (latFitnessCache == null) {
				double totalLatency = getAnts().stream().map(ant -> ant.time).reduce(0l,
						(sum, time) -> sum += curTime - time);
				return 1 - Math.min(1, totalLatency / config.maxTimeSkewMs);
			}
			return latFitnessCache;
		}

		public Set<BigAnt> getAnts() {
			return antToSource.keySet();
		}

		public Set<QuantumFoodSource> getSources() {
			return new LinkedHashSet<>(antToSource.values());
		}
	}

	class PersistentEnsemble {
		// Map from ant id to source position
		Map<String, Position> idToPosition = new LinkedHashMap<>();
		long quantumId;

		List<Ensemble> instances = new LinkedList<>();

		public PersistentEnsemble(Ensemble ensemble) {
			for (Entry<BigAnt, QuantumFoodSource> entry : ensemble.antToSource.entrySet()) {
				idToPosition.put(entry.getKey().id, entry.getValue().position);
			}

			// antIds = ensemble.antToSource.keySet().stream().map(ant -> ant.id).collect(Collectors.toSet());
			// sourcePosition = ensemble.source.position;
			instances.add(ensemble);
			quantumId = ensemble.antToSource.values().iterator().next().quantumId;
		}

		public Ensemble tryMaintain(Collection<BigAnt> ants, List<QuantumFoodSource> foods, long curTime) {
			List<BigAnt> matchedAnts = new LinkedList<>();
			List<QuantumFoodSource> matchedSource = new LinkedList<>();

			// Match ants
			assert (!idToPosition.isEmpty());
			Iterator<String> antIdIt = idToPosition.keySet().iterator();
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
			for (QuantumFoodSource source : foods) {
				for (Position srcPos : idToPosition.values())
					if (PosUtils.isSame(source.position, srcPos) && source.quantumId == quantumId) {
						matchedSource.add(source);
					}
			}

			// Ensemble cannot be maintained as we do not have ants and food to maintain it
			if (matchedAnts.size() != idToPosition.keySet().size()
					|| matchedSource.size() != idToPosition.values().size()) {
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
			double avgAppFitness = instances.stream().mapToDouble(ens -> ens.getAppFitness()).average().getAsDouble();
			double avgLatFitness = instances.stream().mapToDouble(ens -> ens.getLatFitness()).average().getAsDouble();
			return avgLatFitness > 0.5;
		}
	}

	final FitnessMode mode;
	final Set<PersistentEnsemble> persistentEnsembles = new LinkedHashSet<>();

	public PairedHeuristicSolver(FitnessMode mode, Config config) {
		this.mode = mode;
		this.config = config;
	}

	private Collection<Ensemble> generateOptions(List<BigAnt> ants, List<QuantumFoodSource> foods, long curTime) {
		Set<Ensemble> options = new LinkedHashSet<>();

		// Generate all quartets ant, ant, food, food
		// Where ants are different, foods are different and share quantum id
		for (BigAnt antA : ants) {
			for (BigAnt antB : ants) {
				if (antB == antA) {
					continue;
				}
				for (QuantumFoodSource srcA : foods) {
					for (QuantumFoodSource srcB : foods) {
						if (srcA == srcB || srcA.quantumId != srcB.quantumId) {
							continue;
						}

						options.add(new Ensemble(antA, antB, srcA, srcB, curTime));
					}
				}
			}
		}

		return options;
	}

	@Override
	public void solve(Collection<BigAnt> ants, Collection<FoodSource> foods, Position antHill, long curTime) {
		List<BigAnt> remainingAnts = new LinkedList<>(ants);
		List<QuantumFoodSource> remainingFoods = new LinkedList<>();
		for (FoodSource source : foods) {
			remainingFoods.add((QuantumFoodSource) source);
		}

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
				remainingAnts.removeAll(ensemble.getAnts());
				remainingFoods.remove(ensemble.getSources());
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
				if (best == null || ensemble.getAppFitness() > best.getAppFitness()) {
					best = ensemble;
				}
			}

			// Do "knowledge exchange" for best
			best.commit();
			persistentEnsembles.add(new PersistentEnsemble(best));

			// Remove best from remaining
			remainingAnts.removeAll(best.getAnts());
			remainingFoods.remove(best.getSources());
//			System.err.println(best.getSourceFitness() + " " + best.getAntFitness());
		}

		// Set no food position to remaining ants
		for (BigAnt ant : remainingAnts) {
			ant.assignedFood = null;
			ant.currentGoalUtility = 0.0;
		}
	}
}
