package org.basilevs.jstackfilter;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.basilevs.jstackfilter.grammar.jstack.JstackParser;
import org.basilevs.jstackfilter.grammar.jstack.ParseException;


public class Idle {
	private static final List<JavaThread> threads;
	
	static {
		try (InputStream is = Idle.class.getResourceAsStream("idle.txt")) {
			JstackParser subject = new JstackParser(is, "UTF-8");
			threads = List.copyOf(subject.threads());
		} catch (IOException  | ParseException e) {
			throw new AssertionError(e);
		}
	}

	public static boolean isIdle(JavaThread stack) {
		return threads.stream().anyMatch(stack::equalByMethodName);
	}
	
}
