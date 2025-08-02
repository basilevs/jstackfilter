package org.basilevs.jstackfilter.benchmark;

import static org.basilevs.jstackfilter.PushSpliterator.parallel;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.FastChunkSplitter;
import org.basilevs.jstackfilter.JstackParser;
import org.openjdk.jmh.annotations.Benchmark;
import org.openjdk.jmh.annotations.Level;
import org.openjdk.jmh.annotations.Scope;
import org.openjdk.jmh.annotations.Setup;
import org.openjdk.jmh.annotations.State;
import org.openjdk.jmh.annotations.TearDown;
import org.openjdk.jmh.infra.Blackhole;


public class MyBenchmark {
	
	@State(Scope.Thread)
	public static class State1 {
		public Reader input;
		@Setup(Level.Invocation)
		public void setup() {
			input = new InputStreamReader(getClass().getResourceAsStream("/eclipse.txt"), StandardCharsets.UTF_8);
		}
		
		@TearDown(Level.Invocation)
		public void teardown() throws IOException {
			input.close();
		}
	}
	
	@Benchmark
	public void serial(Blackhole bh, State1 state) {
		try (Stream<String> chunks = defaultSplitToChunks(state.input)) {
			consume(chunks, bh);
		}
	}
	
	@Benchmark
	public void serialChunkParallelParse(Blackhole bh, State1 state) {
		try (Stream<String> chunks = defaultSplitToChunks(state.input).parallel()) {
			consume(chunks, bh);
		}
	}


	@Benchmark
	public void parallelChunkSerialParse(Blackhole bh, State1 state) {
		try (Stream<String> chunks = defaultParallel(defaultSplitToChunks(state.input))) {
			consume(chunks, bh);
		}
	}
	
	@Benchmark
	public void parallelChunkParallelParse(Blackhole bh, State1 state) {
		try (Stream<String> chunks = defaultParallel(defaultSplitToChunks(state.input)).parallel()) {
			consume(chunks, bh);
		}
	}
	
	private static void consume(Stream<String> toParse, Blackhole bh) {
		toParse.map(JstackParser::parseThread).flatMap(Optional::stream).forEach(bh::consume);
	}
	
	private static <T> Stream<T> defaultParallel(Stream<T> input) {
		return parallel(input);
	}
	
	private static Stream<String> defaultSplitToChunks(Reader reader) {
		return FastChunkSplitter.splitToChunks(reader);
	}

}
