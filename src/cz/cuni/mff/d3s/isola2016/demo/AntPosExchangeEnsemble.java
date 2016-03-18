package cz.cuni.mff.d3s.isola2016.demo;

import java.util.Map;

import cz.cuni.mff.d3s.deeco.annotations.Ensemble;
import cz.cuni.mff.d3s.deeco.annotations.In;
import cz.cuni.mff.d3s.deeco.annotations.KnowledgeExchange;
import cz.cuni.mff.d3s.deeco.annotations.Membership;
import cz.cuni.mff.d3s.deeco.annotations.PeriodicScheduling;
import cz.cuni.mff.d3s.jdeeco.position.Position;

@Ensemble
@PeriodicScheduling(period = 5000)
public class AntPosExchangeEnsemble {
	@Membership
	public static boolean membership(@In("coord.id") String coordId, @In("member.id") String memberId) {
		return !coordId.equals(memberId);
	}
	
	@KnowledgeExchange
	public static void exchange(@In("coord.otherPos") Map<String, Position> coordOtherPos, @In("member.id") String memberId, @In("member.position") Position memberPosition) {
		coordOtherPos.put(memberId, memberPosition);
	}
}
