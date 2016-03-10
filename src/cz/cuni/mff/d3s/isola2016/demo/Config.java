package cz.cuni.mff.d3s.isola2016.demo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Config {
	public static final int NUM_ANTS = 10;
	public static final double RADIO_RANGE_M = 5;
	public static final int SEED = 42;
	public static final long LIMIT_MS = 180_000;
	public static final long MAX_TIME_SKEW = 30_000;

	public long seed = SEED;
	public long limitMs = LIMIT_MS;
	public int numAnts = NUM_ANTS;
	public double radioRangeM = RADIO_RANGE_M;
	public long maxTimeSkewMs = MAX_TIME_SKEW;

	Config(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("numants", true, "Number of ants");
		options.addOption("radiorange", true, "Range of the radio interface in meters");
		options.addOption("seed", true, "Simulation seed");
		options.addOption("limit", true, "Simulation time limit in ms");
		options.addOption("maxtimeskew", true, "Maximial allowed time skew in ms");
		
		CommandLineParser parser = new GnuParser();
		CommandLine commandLine = parser.parse(options, args);
		
		try {
			seed = Long.parseLong(commandLine.getOptionValue("seed"));
		} catch (Exception e) {
			System.err.println("Seed not provided using default");
		}
		
		try {
			limitMs = Long.parseLong(commandLine.getOptionValue("limit"));
		} catch (Exception e) {
			System.err.println("Time limit not provided using default");
		}
		
		try {
			numAnts = Integer.parseInt(commandLine.getOptionValue("numants"));
		} catch (Exception e) {
			System.err.println("Num ants not provided using default");
		}
		
		try {
			radioRangeM = Double.parseDouble(commandLine.getOptionValue("radiorange"));
		} catch (Exception e) {
			System.err.println("Radio range not provided using default");
		}
		
		try {
			maxTimeSkewMs = Long.parseLong(commandLine.getOptionValue("maxtimeskew"));
		} catch (Exception e) {
			System.err.println("max time skew not provided using default");
		}
	}
}
