package org.basilevs.jstackfilter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.collectors.DistinctBy;

public class ProcessInput {

	public static void main(String[] args) throws IOException {
		try (Reader reader = new InputStreamReader(System.in, StandardCharsets.UTF_8)) {
			ProcessInput.process(reader);
		}
	}

	public static void process(Reader reader) {
		try (Stream<JavaThread> stacks = JstackParser.parseThreads(reader)) {
			Stream<JavaThread> stacksCopy = stacks;
			stacksCopy = stacksCopy.filter(Predicate.not(Known::isKnown));
			stacksCopy = stacksCopy.collect(new DistinctBy<JavaThread>((t1, t2) -> t1.equalByMethodName(t2))).stream();
			stacksCopy.forEach(thread -> System.out.printf("%s\n\n", thread));
		}
	}

}
