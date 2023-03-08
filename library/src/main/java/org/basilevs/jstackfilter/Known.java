package org.basilevs.jstackfilter;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;


/** Widespread threads **/ 
public class Known {
	public static final List<JavaThread> threads;
	
	static {
		try (InputStream is = Known.class.getResourceAsStream("known.txt")) {
			Stream<JavaThread> subject = JstackParser.parseThreads(new InputStreamReader(is, "UTF-8"));
			threads = subject.collect(Collectors.toUnmodifiableList());
		} catch (IOException e) {
			throw new AssertionError(e);
		}
	}

	public static boolean isKnown(JavaThread stack) {
		return threads.stream().anyMatch(stack::equalByMethodName);
	}
	
}
