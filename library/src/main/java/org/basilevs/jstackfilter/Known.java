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
import java.util.stream.Collectors;

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

	private void load(Reader reader) {
		Collection<JavaThread> newThreads = JstackParser.parseThreads(reader)
				.collect(new DistinctBy<JavaThread>((t1, t2) -> t1.equalByMethodName(t2)));
		threads.addAll(
				newThreads.stream().filter(Predicate.not(this::isKnown)).collect(Collectors.toUnmodifiableList()));
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

	public void add(JavaThread another) {
		 threads.add(another);
	}

}
