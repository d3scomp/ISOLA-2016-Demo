package cz.cuni.mff.d3s.isola2016.utils;

import java.util.Locale;

import cz.cuni.mff.d3s.jdeeco.position.Position;

public final class XMLPosition {
	private Position position;

	public XMLPosition(Position position) {
		this.position = position;
	}

	@Override
	public String toString() {
		return String.format(Locale.US, "<x>%f</x><y>%f</y><z>%f</z>", position.x, position.y, position.z);
	}
}