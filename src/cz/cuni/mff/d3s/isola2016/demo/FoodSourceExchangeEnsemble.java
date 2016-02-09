package cz.cuni.mff.d3s.isola2016.demo;

import java.util.List;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.InOut;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.deeco.task.ParamHolder;
import cz.cuni.mff.d3s.isola2016.demo.AntComponent.FoodSourceEx;
import cz.cuni.mff.d3s.isola2016.utils.PosUtils;

@Ensemble
@PeriodicScheduling(period = 5000)
public class FoodSourceExchangeEnsemble {
	@Membership
	public static boolean membership(@In("coord.id") String coordId, @In("member.id") String memberId) {
		return !coordId.equals(memberId);
	}
	
	@KnowledgeExchange
	public static void exchange(@In("coord.foods") List<FoodSourceEx> coordFoods, @InOut("member.foods") ParamHolder<List<FoodSourceEx>> memberFoods) {
		// Mapping coord foods -> member foods
		for(FoodSourceEx coord: coordFoods) {
			// Try to update age
			boolean updated = false;
			for(FoodSourceEx member: memberFoods.value) {
				if(PosUtils.isSame(member.position, coord.position)) {
					updated = true;
					if(member.timestamp < coord.timestamp) {
						member.timestamp = coord.timestamp;
						member.portions = coord.portions;
					}
				}
			}
			
			if(updated) {
				continue;
			}
			
			// Add new record
			memberFoods.value.add(coord);
		}
	}
}
