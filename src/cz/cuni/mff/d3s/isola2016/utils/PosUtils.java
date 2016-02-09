package cz.cuni.mff.d3s.isola2016.utils;

import java.util.Random;

import cz.cuni.mff.d3s.jdeeco.position.Position;

public class PosUtils {
	/**
	 * Generates random position in circle around base
	 * 
	 * @param rand
	 *            Random data source
	 * @param x
	 *            Base x coordinate
	 * @param y
	 *            Base y coordinate
	 * @param diameter
	 *            Max distance from base
	 * @return Generated position
	 */
	public static Position getRandomPosition(Random rand, double x, double y, double diameter) {
		double px = x + ((rand.nextDouble() - 0.5) * diameter);
		double py = y + ((rand.nextDouble() - 0.5) * diameter);
		return new Position(px, py);
	}
	
	/**
	 * Distance at which the positions are considered same
	 */
	public static double SAME_POS_DIST_M = 0.01;
	
	public static boolean isSame(Position a, Position b) {
		if(a == null ^ b == null) {
			return false;
		}
		
		return a == b || a.euclidDistanceTo(b) < SAME_POS_DIST_M;
	}
}
