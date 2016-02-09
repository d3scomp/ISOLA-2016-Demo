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
}
