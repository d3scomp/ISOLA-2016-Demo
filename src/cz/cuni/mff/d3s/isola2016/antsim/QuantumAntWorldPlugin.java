package cz.cuni.mff.d3s.isola2016.antsim;

import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

import cz.cuni.mff.d3s.isola2016.antsim.BigAntPlugin.State;
import cz.cuni.mff.d3s.isola2016.demo.Config;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.jdeeco.position.Position;

/**
 * Quantum version with paired food sources that require two ants to lock on paired sources to do pickup
 * 
 * @author Vladimir Matena <matena@d3s.mff.cuni.cz>
 *
 */
public class QuantumAntWorldPlugin extends AbstractAntWorldPlugin {
	public QuantumAntWorldPlugin(Position antHill, Random rand, Config config) {
		super(antHill, rand, config);
	}
	
	private BigAntPlugin getHelper(QuantumFoodSource source) {
		for (BigAntPlugin ant : bigAnts) {
			if(PosUtils.isSame(ant.position, source.position)) {
				return ant;
			}
		}
		return null;
	}

	@Override
	protected void resolveLocked() {
		// Resolve Locked -> Pulling
		for (BigAntPlugin ant : bigAnts) {
			// Ant not locked
			if (ant.state != State.Locked) {
				continue;
			}

			// Get locked source
			QuantumFoodSource source = (QuantumFoodSource) getFoodSourceAt(ant.getPosition());
			if (source == null) {
				ant.state = State.Free;
			}

			// There is no helper
			BigAntPlugin helper = getHelper(source.other);
			if (helper == null) {
				continue;
			}

			// Create food piece and start polling
			if (--source.portions == 0) {
				removeFoodSource(source);
			}
			if (--source.other.portions == 0) {
				removeFoodSource(source.other);
			}
			
			giveAntFoodPiece(ant);
			giveAntFoodPiece(helper);
		}
	}
	
	private void giveAntFoodPiece(BigAntPlugin ant) {
		FoodPiece piece = new FoodPiece(ant.getPosition(), ant);
		foodPieces.add(piece);;
		ant.state = State.Pulling;
		ant.pulledFoodPiece = piece;
	}

	@Override
	protected void maintainFoodSourcePopulation() {
		// Add new food sources
		if (foodSources.size() < SOURCE_COUNT) {
			Set<QuantumFoodSource> sources = QuantumFoodSource.createQuantumFoodPair(
					PosUtils.getRandomPosition(rand, antHill, FOOD_SOURCE_SPAWN_DIAMETER_M),
					PosUtils.getRandomPosition(rand, antHill, FOOD_SOURCE_SPAWN_DIAMETER_M), FOOD_SOURCE_CAPACITY);
			for (QuantumFoodSource source : sources) {
				addFoodSource(source);
			}
		}

		// Remove food sources
		List<FoodSource> toRemove = new LinkedList<>();
		for (FoodSource source : foodSources) {
			QuantumFoodSource qsource = (QuantumFoodSource)source;
			if (rand.nextDouble() < PER_SOURCE_REMOVE_PROBABILITY_PER_S / (1000 / SIM_STEP_MS)) {
				toRemove.add(qsource);
				toRemove.add(qsource.other);
			}
		}
		for (FoodSource source : toRemove) {
			removeFoodSource(source);
		}
	}
}
