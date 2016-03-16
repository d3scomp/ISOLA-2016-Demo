package cz.cuni.mff.d3s.isola2016.demo;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

public class Config {
	public long seed;
	public long limitMs;
	public int numSmallAnts;
	public int numBigAnts;
	public double radioRangeM;
	public long maxTimeSkewMs;

	Config(String[] args) throws ParseException {
		Options options = new Options();
		options.addOption("numbigants", true, "Number of bigants");
		options.addOption("numsmallants", true, "Number of small ants");
		options.addOption("radiorange", true, "Range of the radio interface in meters");
		options.addOption("seed", true, "Simulation seed");
		options.addOption("limit", true, "Simulation time limit in ms");
		options.addOption("maxtimeskew", true, "Maximial allowed time skew in ms");

		CommandLineParser parser = new GnuParser();
		CommandLine commandLine = parser.parse(options, args);

		seed = Long.parseLong(commandLine.getOptionValue("seed"));
		limitMs = Long.parseLong(commandLine.getOptionValue("limit"));
		numBigAnts = Integer.parseInt(commandLine.getOptionValue("numbigants"));
		numSmallAnts = Integer.parseInt(commandLine.getOptionValue("numsmallants"));
		radioRangeM = Double.parseDouble(commandLine.getOptionValue("radiorange"));
		maxTimeSkewMs = Long.parseLong(commandLine.getOptionValue("maxtimeskew"));
	}
}
