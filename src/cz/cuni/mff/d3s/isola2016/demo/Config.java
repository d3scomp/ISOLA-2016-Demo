package cz.cuni.mff.d3s.isola2016.demo;

import java.lang.reflect.Field;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import cz.cuni.mff.d3s.deeco.runtime.DEECoRuntimeException;

public class Config {
	public Long seed;
	public Long limitMs;
	public Integer numSmallAnts;
	public Integer numBigAnts;
	public Double radioRangeM;
	public Long maxTimeSkewMs;
	public Long logIntervalMs;
	public Boolean useRebroadcasting;
	public Double rebroadcastRangeM;
	public Long rebroadcastDelayMs;
	public String mode;

	Config(String[] args) throws ParseException {
		Options options = new Options();
		
		// Define options for fields
		for(Field field: this.getClass().getDeclaredFields()) {
			options.addOption(field.getName(), true, field.getName());
		}

		CommandLineParser parser = new GnuParser();
		CommandLine commandLine = parser.parse(options, args);
		
		// Parser values for fields
		for(Field field: this.getClass().getDeclaredFields()) {
			String stringValue = commandLine.getOptionValue(field.getName());
			Object value = null;
			
			if(field.getType().equals(String.class)) {
				value = stringValue;
			} else if(field.getType().equals(Boolean.class)) {
				value = Boolean.parseBoolean(stringValue);
			} else if(field.getType().equals(Integer.class)) {
				value = Integer.parseInt(stringValue);
			} else if(field.getType().equals(Long.class)) {
				value = Long.parseLong(stringValue);
			} else if(field.getType().equals(Double.class)) {
				value = Double.parseDouble(stringValue);
			} else {
				throw new DEECoRuntimeException("Config parsing not defined for type " + field.getType().getName());
			}
			
			if(value == null) {
				throw new RuntimeException("Missing parameter \"" + field.getName() + "\"");
			}
						
			try {
				field.set(this, value);
			} catch (Exception e) {
				throw new DEECoRuntimeException("Config parsing failed", e);
			}
		}
	}
}
