package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class BruteforceSolver implements AntAssignmetSolver {
	static class Pair {
		AntInfo a;
		AntInfo b;

		public Pair(AntInfo a, AntInfo b) {
			this.a = a;
			this.b = b;
		}

		@Override
		public String toString() {
			return a + "-" + b;
		}
	}

	static class Triplet {
		AntInfo a;
		AntInfo b;
		FoodSource c;

		public Triplet(Pair p, FoodSource c) {
			this.a = p.a;
			this.b = p.b;
			this.c = c;
		}

		@Override
		public String toString() {
			return a + "-" + b + "-" + c;
		}
	}

	static class C {
		Stack<Pair> pairs = new Stack<>();
		Stack<Triplet> triplets = new Stack<>();
		Stack<AntInfo> toPair = new Stack<>();
		Stack<FoodSource> toTriplet = new Stack<>();

		public C(Collection<Triplet> triplets, Collection<Pair> pairs, Collection<AntInfo> toPair,
				Collection<FoodSource> toTriplet) {
			this.pairs.addAll(pairs);
			this.triplets.addAll(triplets);
			this.toPair.addAll(toPair);
			this.toTriplet.addAll(toTriplet);
		}

		public void printPairs() {
			StringBuilder builder = new StringBuilder();
			for (Pair p : pairs) {
				builder.append(p.toString());
				builder.append(System.lineSeparator());
			}
			System.out.println(builder.toString());
		}

		public void printTriplets() {
			StringBuilder builder = new StringBuilder();
			for (Triplet t : triplets) {
				builder.append(t.toString());
				builder.append(System.lineSeparator());
			}
			System.out.println(builder.toString());
		}
	}

	public static Collection<Collection<Triplet>> combine(Collection<AntInfo> ants, Collection<FoodSource> foods) {
		if (foods.isEmpty()) {
			return new ArrayList<>();
		}

		Stack<C> toPair = new Stack<>();
		Stack<C> toTriple = new Stack<>();
		toPair.add(new C(Collections.emptyList(), Collections.emptyList(), ants, foods));

		List<C> done = new LinkedList<>();

		// Generate all possible ant pairs
		while (!toPair.isEmpty()) {
			C c = toPair.pop();

			if (c.toPair.size() < 2) {
				toTriple.add(c);
				continue;
			}

			AntInfo i = c.toPair.pop();

			for (AntInfo j : c.toPair) {
				if (i == j) {
					continue;
				}

				C n = new C(c.triplets, c.pairs, c.toPair, c.toTriplet);
				n.pairs.add(new Pair(i, j));
				n.toPair.remove(j);

				toPair.push(n);
			}
		}
		/*
		 * System.out.println("Pairs:"); for(C c: toTriple) { System.out.println("Comb:"); c.printPairs(); }
		 */
		System.out.print(" P: " + toTriple.size() + " ");
		System.out.flush();

		// Generate all possible pairs of ant pair and food
		long lastTime = System.currentTimeMillis();
		while (!toTriple.isEmpty()) {
			if (System.currentTimeMillis() - lastTime > 5000) {
				lastTime = System.currentTimeMillis();
				System.out.println("totriple: " + toTriple.size() + " done: " + done.size());
			}

			C c = toTriple.pop();

			if (c.toTriplet.isEmpty() || c.pairs.isEmpty()) {
				done.add(c);
				continue;
			}

			Pair i = c.pairs.pop();
			for (FoodSource j : c.toTriplet) {
				C n = new C(c.triplets, c.pairs, c.toPair, c.toTriplet);
				n.triplets.add(new Triplet(i, j));
				toTriple.push(n);
			}
		}

		/*
		 * System.out.println("Triplets:"); for(C c: done) { System.out.println("Comb:"); c.printTriplets(); }
		 */
		System.out.print(" T: " + done.size() + " ");
		System.out.flush();

		Collection<Collection<Triplet>> ret = new LinkedList<>();

		for (C c : done) {
			if (!c.triplets.isEmpty()) {
				ret.add(c.triplets);
			}
		}

		return ret;
	}

	private static double getBadnessAntAntFood(AntInfo a, AntInfo b, FoodSource f) {
		return a.position.euclidDistanceTo(f.position) + b.position.euclidDistanceTo(f.position);
	}

	private static double getSolutionBadness(Collection<Triplet> triplets) {
		double badness = 0;
		for (Triplet t : triplets) {
			badness += getBadnessAntAntFood(t.a, t.b, t.c);
		}
		return badness;
	}

	public Position solve(Collection<AntInfo> ants, Collection<FoodSource> foods, AntInfo localAnt, Position antHill) {
		// Generate all possible solutions
		System.out.print("Fake intelligence: Ants: " + ants.size() + " foods: " + foods.size() + " >>> ");
		System.out.flush();
		Collection<Collection<Triplet>> combined = BruteforceSolver.combine(ants, foods);
		System.out.print(combined.size() + " alternatives --- ");

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
		if (bestSolution != null) {
			for (Triplet t : bestSolution) {
				if (t.a == localAnt || t.b == localAnt) {
					sourceAssignedToLocalAnt = t.c;
				}
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
		return assignedPosition;
	}
}
