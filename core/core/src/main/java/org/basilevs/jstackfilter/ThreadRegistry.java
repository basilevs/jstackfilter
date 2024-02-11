package org.basilevs.jstackfilter;

import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.internal.OS;

/**
 * This class represents a Set of JavaThread objects. The state of the Set is
 * persisted to disk when the application closes and is restored on start. This
 * ensures that the data within the Set remains consistent across multiple
 * sessions of the application.
 */
public final class ThreadRegistry implements Closeable {
	public final List<JavaThread> threads = new CopyOnWriteArrayList<>();
	private final Path configurationFile;
	private static final Collector<JavaThread, Collection<JavaThread>, Collection<JavaThread>> DISTINCT_BY_NAME = new DistinctBy<JavaThread>(
			(t1, t2) -> t1.equalByMethodName(t2));

	/** Threads known to be idle uninteresting **/
	public static ThreadRegistry idle() throws IOException {
		return new ThreadRegistry(OS.detect().configurationDirectory().resolve("idle.txt"), "idle.txt");
	}

	/**
	 * 
	 * @param configurationFile - a file on filesystem to store state, MUST be
	 *                          writable, MAY exist, is the flee exists, it must be
	 *                          in the format of jstack output
	 * @param resource          - a resource name stored in ThreadRegistry's
	 *                          package, MUST exist
	 * @throws IOException - when preconditions are not met
	 */
	public ThreadRegistry(Path configurationFile, String resource) throws IOException {
		this.configurationFile = Objects.requireNonNull(configurationFile);
		try (InputStream defaultConfiguration = ThreadRegistry.class.getResourceAsStream(resource)) {
			if (defaultConfiguration == null)
				throw new IllegalArgumentException("Configuration " + resource + " does not exist");
			if (Files.exists(configurationFile)) {
				try (Reader is = Files.newBufferedReader(configurationFile, StandardCharsets.UTF_8)) {
					load(is);
				}
			} else {
				load(new InputStreamReader(defaultConfiguration, StandardCharsets.UTF_8));
			}
		}
	}

	public void load(Reader reader) {
		addAll(JstackParser.parseThreads(reader));
	}

	private void save(Writer output) {
		// Can't do parallel streams, as it will change the order of the threads and the
		// file change will be harder to analyze.
		for (JavaThread thread : threads.stream().collect(DISTINCT_BY_NAME)) {
			try {
				output.write(thread.toString());
				output.write("\n\n");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public boolean contains(JavaThread stack) {
		return threads.parallelStream().anyMatch(stack::equalByMethodName);
	}

	@Override
	public void close() throws IOException {
		Files.createDirectories(configurationFile.getParent());

		try (Writer writer = Files.newBufferedWriter(configurationFile)) {
			save(writer);
		}
	}

	public void addAll(Stream<JavaThread> newThreads) {
		var tmp = newThreads.peek(this::checkThread).collect(DISTINCT_BY_NAME);
		threads.addAll(tmp.stream().filter(Predicate.not(this::contains)).collect(Collectors.toUnmodifiableList()));
		// Due to concurrency, threads will contain some duplicates, but there won't be
		// a lot, and they won't affect performance much
	}

	public void removeAll(Stream<JavaThread> threads2) {
		var tmp = threads2.collect(DISTINCT_BY_NAME);
		threads.removeIf(idle -> tmp.stream().anyMatch(idle::equalByMethodName));
	}

	private void checkThread(JavaThread input) {
		var dump = input.toString();
		JavaThread parsed = JstackParser.parseThread(dump).get();
		if (!input.equalByMethodName(parsed)) {
			throw new IllegalArgumentException("Can't properly serialize " + input);
		}
	}

}
