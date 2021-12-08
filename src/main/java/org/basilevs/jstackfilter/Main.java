package org.basilevs.jstackfilter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.function.Predicate;
import java.util.stream.Stream;

public class Main {

	public static void main(String[] args) throws IOException {
		try (Reader reader = new InputStreamReader(System.in, StandardCharsets.UTF_8)) {
			try (Stream<JavaThread> stacks = JstackParser.parse(reader)) {
				Stream<JavaThread> stacksCopy = stacks;
				stacksCopy = stacksCopy.filter(Predicate.not(Known::isKnown));
				stacksCopy.forEach(thread -> System.out.printf("%s\n\n", thread));
			}
		}

	}

}
