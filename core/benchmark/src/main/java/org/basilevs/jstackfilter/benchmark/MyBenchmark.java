package org.basilevs.jstackfilter.benchmark;

import java.io.Reader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;
import java.util.Scanner;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.JstackParser;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.infra.Blackhole;

@State(Scope.Benchmark)
public class MyBenchmark {
	public String input;
	

	@Setup(Level.Trial)
	public void setup() {
		try (Scanner scanner = new Scanner(getClass().getResourceAsStream("/eclipse.txt"), StandardCharsets.UTF_8)) {
			input = scanner.useDelimiter("\\A").next();
		}
		if (input.contains("java.lang.d=0x3d2c")) {
			throw new IllegalArgumentException();
		}
	}
	
	@Benchmark
	public void serialParse(Blackhole bh) {
		try (StringReader stringReader = new StringReader(input);
				Stream<JavaThread> threads = JstackParser.splitToChunks((Reader) stringReader)
						.map(JstackParser::parseThread).flatMap(Optional::stream)) {
			threads.forEach(bh::consume);
		}
	}

	@Benchmark
	public void parseParallel(Blackhole bh) {
		try (StringReader stringReader = new StringReader(input);
				Stream<JavaThread> threads = JstackParser.parallel(JstackParser.splitToChunks((Reader) stringReader))
						.map(JstackParser::parseThread).flatMap(Optional::stream)) {
			threads.forEach(bh::consume);
		}
	}

}
