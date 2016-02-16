package cz.cuni.mff.d3s.isola2016.utils;

import java.io.FileWriter;
import java.io.IOException;

import cz.cuni.mff.d3s.deeco.timer.CurrentTimeProvider;
import cz.cuni.mff.d3s.isola2016.demo.AntLogRecord;

public class SimpleLogger {
	static {
		try {
			FileWriter w = new FileWriter("logs/simple.xml");
			w.write("<log>");
			writer = w;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	static FileWriter writer;

	public static CurrentTimeProvider clock;

	public synchronized static void log(AntLogRecord record) {
		String rec = String.format("<event time=\"%d\" id=\"%s\" eventType=\"%s\" ><positon>%s</positon></event>",
				clock.getCurrentMilliseconds(), record.getId(), record.getClass().getName(), record.xmlPos.toString());
	
		try {
			writer.write(rec + System.lineSeparator());
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static void close() {
		try {
			writer.write("</log>");
			writer.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
