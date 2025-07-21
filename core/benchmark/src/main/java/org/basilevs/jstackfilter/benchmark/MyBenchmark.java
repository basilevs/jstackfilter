package org.basilevs.jstackfilter.benchmark;

import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Scanner;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.JavaThread;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;

@State(Scope.Benchmark)
public class MyBenchmark {
	public String input;

	@Setup
	public void setup() {
		try (Scanner scanner = new Scanner(getClass().getResourceAsStream("/eclipse.txt"), StandardCharsets.UTF_8)) {
			input = scanner.useDelimiter("\\A").next();
		}
	}
	
    @Benchmark
    public List<JavaThread> parse() {
    	try (StringReader stringReader = new StringReader(input);
    			Stream<JavaThread> threads = org.basilevs.jstackfilter.JstackParser.parseThreads(stringReader)) {
			return org.basilevs.jstackfilter.JstackParser.parseThreads(stringReader).toList();
		}
    }

}
