package cz.cuni.mff.d3s.isola2016.demo;

public enum Mode {
	Searching, ToFood, Grip, Gripped, Release, Pulling;
	
	public boolean isPlanable() {
		switch(this) {
		case Searching:
		case ToFood:
		case Grip:
		case Gripped:
			return true;
		default:
			return false;
		}
	}
}
