package cz.cuni.mff.d3s.isola2016.utils;

import java.io.FileWriter;
import java.io.IOException;
import java.util.Map.Entry;

import cz.cuni.mff.d3s.deeco.timer.CurrentTimeProvider;
import cz.cuni.mff.d3s.isola2016.demo.AntLogRecord;

public class SimpleLogger {
	static {
		try {
			FileWriter w = new FileWriter("logs/simple-" + System.currentTimeMillis() + ".xml");
			w.write("<log>" + System.lineSeparator());
			writer = w;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static FileWriter writer;

	public static CurrentTimeProvider clock;

	public synchronized static void log(AntLogRecord record) {
		StringBuilder builder = new StringBuilder();
		builder.append(String.format("<event time=\"%d\" id=\"%s\" eventType=\"%s\" >",
				clock.getCurrentMilliseconds(), record.getId(), record.getClass().getName()));
		
		for(Entry<String, Object> entry: record.getValues().entrySet()) {
			builder.append(String.format("<%s>", entry.getKey()));
			builder.append(entry.getValue().toString());
			builder.append(String.format("</%s>", entry.getKey()));
		}
		
		builder.append("</event>");
		builder.append(System.lineSeparator());
			
		try {
			writer.write(builder.toString());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void close() {
		try {
			writer.write(System.lineSeparator() + "</log>");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
