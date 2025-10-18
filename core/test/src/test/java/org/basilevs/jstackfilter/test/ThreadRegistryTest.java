package org.basilevs.jstackfilter.test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.ThreadRegistry;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TemporaryFolder;

public class ThreadRegistryTest {

	private static final List<JavaThread> threads;
	private static final JavaThread UNKNOWN;
	private static final String REGISTRY_RESOURCE = "idle";
	static {
		threads = Utils.readThreadResource(JstackParserTest.class, "eclipse.txt");
		JavaThread first = threads.stream().filter(t -> t.toString().contains("[0x00000014682fe000]")).findAny().get();
		List<Frame> frames = new ArrayList<>(first.frames());
		frames.remove(0);
		UNKNOWN = new JavaThread(first.name(), 0, first.state(), frames);
	}

	@Rule
	public final TemporaryFolder temp = new TemporaryFolder();

	private ThreadRegistry subject;

	private Path configurationFile;

	@Before
	public void before() throws IOException {
		configurationFile = temp.getRoot().toPath().resolve(REGISTRY_RESOURCE+".txt");
		try (Stream<JavaThread> is = ThreadRegistry.readResource(REGISTRY_RESOURCE)) {
			subject = new ThreadRegistry(configurationFile, is);
		}
	}

	@Test
	public void test() {
		JavaThread first = threads.iterator().next();
		Assert.assertTrue(subject.contains(first));
		List<Frame> frames = new ArrayList<>(first.frames());
		JavaThread another = new JavaThread(first.name(), 0, first.state(), frames);
		Assert.assertTrue(subject.contains(another));
		another = new JavaThread("meh", 0, first.state(), frames);
		Assert.assertTrue(subject.contains(another));
		another = new JavaThread(first.name(), 0, "boo", frames);
		Assert.assertTrue(subject.contains(another));
		another = UNKNOWN;
		Assert.assertFalse(subject.contains(another));
	}

	@Test
	public void persistOverSaveAndLoad() throws IOException {
		Assert.assertFalse(Files.exists(configurationFile));
		Assert.assertFalse(subject.contains(UNKNOWN));
		subject.addAll(Stream.of(UNKNOWN));
		Assert.assertTrue(subject.contains(UNKNOWN));
		subject.close();
		try (Stream<JavaThread> is = ThreadRegistry.readResource(REGISTRY_RESOURCE)) {
			subject = new ThreadRegistry(configurationFile, is);
		}
		Assert.assertTrue(subject.contains(UNKNOWN));

	}

	@Test
	public void knownThreadsAreUnique() {
		List<JavaThread> collected;
		try (Stream<JavaThread> threads = ThreadRegistry.readResource(REGISTRY_RESOURCE)) {
			collected = threads.collect(Collectors.toCollection(ArrayList::new));
		}
		while (!collected.isEmpty()) {
			JavaThread first = collected.remove(0);
			for (JavaThread i : collected) {
				boolean matches = i.equalByMethodName(first);
				Assert.assertFalse("" + first + "\n" + i, matches);
			}
		}
	}

}
