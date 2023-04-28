package org.basilevs.jstackfilter.test;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.JstackParser;
import org.basilevs.jstackfilter.Known;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class KnownTest {

	private static final List<JavaThread> threads;
	private static final JavaThread UNKNOWN;
	static {
		try (InputStream is = JstackParserTest.class.getResourceAsStream("eclipse.txt")) {
			threads = JstackParser.parseThreads(new InputStreamReader(is, StandardCharsets.UTF_8)).collect(Collectors.toList());
		} catch (IOException e) {
			throw new AssertionError(e);
		}
		JavaThread first = threads.iterator().next();
		List<Frame> frames = new ArrayList<>(first.frames());
		frames.remove(0);
		UNKNOWN = new JavaThread(first.name(), 0, first.state(), frames);

	}

	@Rule
	public final TemporaryFolder temp = new TemporaryFolder();

	private Known subject;

	private Path configurationFile;

	@Before
	public void before() throws IOException {
		configurationFile = temp.getRoot().toPath().resolve("known.txt");
		subject = new Known(configurationFile);
	}

	@Test
	public void test() {
		JavaThread first = threads.iterator().next();
		Assert.assertTrue(subject.isKnown(first));
		List<Frame> frames = new ArrayList<>(first.frames());
		JavaThread another = new JavaThread(first.name(), 0, first.state(), frames);
		Assert.assertTrue(subject.isKnown(another));
		another = new JavaThread("meh", 0, first.state(), frames);
		Assert.assertTrue(subject.isKnown(another));
		another = new JavaThread(first.name(), 0, "boo", frames);
		Assert.assertTrue(subject.isKnown(another));
		another = UNKNOWN;
		Assert.assertFalse(subject.isKnown(another));
	}

	@Test
	public void persistOverSaveAndLoad() throws IOException {
		Assert.assertFalse(Files.exists(configurationFile));
		Assert.assertFalse(subject.isKnown(UNKNOWN));
		subject.add(UNKNOWN);
		Assert.assertTrue(subject.isKnown(UNKNOWN));
		subject.close();
		subject = new Known(configurationFile);
		Assert.assertTrue(subject.isKnown(UNKNOWN));

	}

	@Test
	public void knownThreadsAreUnique() {
		List<JavaThread> threads = new ArrayList<>(subject.threads);
		while (!threads.isEmpty()) {
			JavaThread first = threads.remove(0);
			for (JavaThread i : threads) {
				boolean matches = i.equalByMethodName(first);
				Assert.assertFalse("" + first + "\n" + i, matches);
			}
		}
	}

}
