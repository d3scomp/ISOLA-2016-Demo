package cz.cuni.mff.d3s.isola2016.antsim;

import cz.cuni.mff.d3s.deeco.runtime.DEECoContainer;
import cz.cuni.mff.d3s.deeco.runtime.PluginInitFailedException;

/**
 * Represents one small ant in simulation
 * 
 * Small ants act just as communication relays
 * 
 * @author Vladimir Matena <matena@d3s.mff.cuni.cz>
 *
 */
public class SmallAntPlugin extends AntPlugin {
	// Realistic ant speed is 0.07 m/s
	// Mechanic ants are a lot faster
	public static double SPEED_M_PER_S = 0.5;
	
	@Override
	public void init(DEECoContainer container) throws PluginInitFailedException {
		super.init(container);
		world.registerAnt(this);
	}
}
