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
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.internal.OS;

/** Widespread threads **/
public final class Known implements Closeable {
	public final List<JavaThread> threads = new CopyOnWriteArrayList<>();
	private final Path configurationFile;

	public Known() throws IOException {
		this(OS.detect().configurationDirectory().resolve("known.txt"));
	}

	public Known(Path configurationFile) throws IOException {
		this.configurationFile = Objects.requireNonNull(configurationFile);
		if (Files.exists(configurationFile)) {
			try (Reader is = Files.newBufferedReader(configurationFile, StandardCharsets.UTF_8)) {
				load(is);
			}
		} else {
			try (InputStream is = Known.class.getResourceAsStream("known.txt")) {
				load(new InputStreamReader(is, StandardCharsets.UTF_8));
			}
		}
	}

	public void load(Reader reader) {
		addAll(JstackParser.parseThreads(reader));
	}

	private void save(Writer output) {
		for (JavaThread thread : threads) {
			try {
				output.write(thread.toString());
				output.write("\n\n");
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		}
	}

	public boolean isKnown(JavaThread stack) {
		return threads.stream().anyMatch(stack::equalByMethodName);
	}

	@Override
	public void close() throws IOException {
		Files.createDirectories(configurationFile.getParent());

		try (Writer writer = Files.newBufferedWriter(configurationFile)) {
			save(writer);
		}
	}

	public void addAll(Stream<JavaThread> newThreads) {
		var tmp = newThreads.peek(this::checkThread).collect(new DistinctBy<JavaThread>((t1, t2) -> t1.equalByMethodName(t2)));
		threads.addAll(tmp.stream().filter(Predicate.not(this::isKnown)).collect(Collectors.toUnmodifiableList()));
		// Due to concurrency, threads will contain some duplicates, but there won't be a lot, and they won't affect performance much
	}

	private void checkThread(JavaThread input) {
		var dump = input.toString();
		JavaThread parsed = JstackParser.parseThread(dump).get();
		if (!input.equalByMethodName(parsed)) {
			throw new IllegalArgumentException("Can't properly serialize " + input);
		}
	}

}
