package cz.cuni.mff.d3s.isola2016.ensemble;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Stack;

import cz.cuni.mff.d3s.isola2016.antsim.FoodSource;

public class Combiner {
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
		
		public C(Collection<Triplet> triplets, Collection<Pair> pairs, Collection<AntInfo> toPair, Collection<FoodSource> toTriplet) {
			this.pairs.addAll(pairs);
			this.triplets.addAll(triplets);
			this.toPair.addAll(toPair);
			this.toTriplet.addAll(toTriplet);
		}
		
		public void printPairs() {
			StringBuilder builder = new StringBuilder();
			for(Pair p: pairs) {
				builder.append(p.toString());
				builder.append(System.lineSeparator());
			}
			System.out.println(builder.toString());
		}
		
		public void printTriplets() {
			StringBuilder builder = new StringBuilder();
			for(Triplet t: triplets) {
				builder.append(t.toString());
				builder.append(System.lineSeparator());
			}
			System.out.println(builder.toString());
		}
	}

	public static Collection<Collection<Triplet>> combine(Collection<AntInfo> ants, Collection<FoodSource> foods) {
		if(foods.isEmpty()) {
			return new ArrayList<>();
		}
		
		Stack<C> toPair = new Stack<>();
		Stack<C> toTriple = new Stack<>();
		toPair.add(new C(Collections.emptyList(), Collections.emptyList(), ants, foods));
		
		List<C> done = new LinkedList<>();
		
		// Generate all possible ant pairs
		while(!toPair.isEmpty()) {
			C c = toPair.pop();
			
			if(c.toPair.size() < 2) {
				toTriple.add(c);
				continue;
			}
			
			AntInfo i = c.toPair.pop();
			
			for(AntInfo j: c.toPair) {
				if(i == j) {
					continue;
				}
		
				C n = new C(c.triplets, c.pairs, c.toPair, c.toTriplet);
				n.pairs.add(new Pair(i, j));
				n.toPair.remove(j);
			
				toPair.push(n);
			}
		}
		/*
		System.out.println("Pairs:");
		for(C c: toTriple) {
			System.out.println("Comb:");
			c.printPairs();
		}*/
		System.out.println("Total: " + toTriple.size() + " pair configurations");
		
		// Generate all possible pairs of ant pair and food
		while(!toTriple.isEmpty()) {
			C c = toTriple.pop();
			
			if(c.toTriplet.isEmpty() || c.pairs.isEmpty()) {
				done.add(c);
				continue;
			}
			
			Pair i = c.pairs.pop();
			for(FoodSource j: c.toTriplet) {
				C n = new C(c.triplets, c.pairs, c.toPair, c.toTriplet);
				n.triplets.add(new Triplet(i, j));
				toTriple.push(n);
			}
		}
		
		/*System.out.println("Triplets:");
		for(C c: done) {
			System.out.println("Comb:");
			c.printTriplets();
		}*/
		System.out.println("Total: " + done.size() + " triplet configurations");
		
		Collection<Collection<Triplet>> ret = new LinkedList<>();
		
		for(C c: done) {
			if(!c.triplets.isEmpty()) {
				ret.add(c.triplets);
			}
		}
		
		return ret;
	}
}
