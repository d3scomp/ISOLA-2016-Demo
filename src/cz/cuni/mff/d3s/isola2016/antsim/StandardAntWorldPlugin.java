package cz.cuni.mff.d3s.isola2016.antsim;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;

import cz.cuni.mff.d3s.isola2016.antsim.BigAntPlugin.State;
import cz.cuni.mff.d3s.isola2016.demo.Config;
import cz.cuni.mff.d3s.isola2016.demo.FoodSource;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;
import cz.cuni.mff.d3s.jdeeco.position.Position;

public class StandardAntWorldPlugin extends AbstractAntWorldPlugin {
	public StandardAntWorldPlugin(Position antHill, Random rand, Config config) {
		super(antHill, rand, config);
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
			FoodSource source = getFoodSourceAt(ant.getPosition());
			if (source == null) {
				ant.state = State.Free;
			}

			// There is no helper
			Collection<BigAntPlugin> helpers = getHelpers(ant);
			if (helpers.size() < HELPERS_NEEDED) {
				continue;
			}

			// Create food piece and start polling
			if (--source.portions == 0) {
				removeFoodSource(source);
			}
			FoodPiece piece = new FoodPiece(ant.getPosition(), ant);
			foodPieces.add(piece);
			ant.state = State.Pulling;
			ant.pulledFoodPiece = piece;
			for (BigAntPlugin helper : helpers) {
				helper.state = State.Free;
			}
		}
	}
	
	@Override
	protected void maintainFoodSourcePopulation() {
		// Add new food sources
		if (foodSources.size() < SOURCE_COUNT) {
			addFoodSource(new FoodSource(PosUtils.getRandomPosition(rand, antHill, FOOD_SOURCE_SPAWN_DIAMETER_M),
					FOOD_SOURCE_CAPACITY, 0));
		}

		// Remove food sources
		List<FoodSource> toRemove = new LinkedList<>();
		for (FoodSource source : foodSources) {
			if (rand.nextDouble() < PER_SOURCE_REMOVE_PROBABILITY_PER_S / (1000 / SIM_STEP_MS)) {
				toRemove.add(source);
			}
		}
		for (FoodSource source : toRemove) {
			removeFoodSource(source);
		}
	}
	
	/**
	 * Returns helper ant
	 * 
	 */
	private Collection<BigAntPlugin> getHelpers(BigAntPlugin ant) {
		Collection<BigAntPlugin> helpers = new LinkedHashSet<>();
		for (BigAntPlugin helper : bigAnts) {
			if (helper != ant && helper.state == State.Locked
					&& PosUtils.isSame(helper.getPosition(), ant.getPosition())) {
				helpers.add(helper);
			}
		}
		return helpers;
	}
}
