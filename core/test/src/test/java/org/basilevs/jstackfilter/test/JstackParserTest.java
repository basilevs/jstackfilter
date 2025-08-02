package org.basilevs.jstackfilter.test;

import static org.basilevs.jstackfilter.PushSpliterator.parallel;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Optional;
import java.util.function.BiPredicate;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.basilevs.jstackfilter.FastChunkSplitter;
import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.JstackParser;
import org.junit.Assert;
import org.junit.Test;

public class JstackParserTest {

	@Test
	public void frame1() throws IOException {
		String data = Utils.readClassResource(JstackParserTest.class, "eclipse.txt");
		Collection<JavaThread> threads = JstackParser.parseThreads(new StringReader(data)).collect(Collectors.toList());
		Assert.assertTrue(threads.stream().map(JavaThread::name)
				.anyMatch(Predicate.isEqual("Active Thread: Equinox Container: 4201e9ce-3eab-4ab0-8431-f4be70328a4d")));
		JavaThread first = threads.iterator().next();
		Frame firstFrame = first.frames().get(0);
		Assert.assertEquals(firstFrame.method(), "org.eclipse.swt.internal.win32.OS.WaitMessage");
		Assert.assertEquals(firstFrame.location(), "Native Method");
		Frame secondFrame = first.frames().get(1);
		Assert.assertEquals(secondFrame.method(), "org.eclipse.swt.widgets.Display.sleep");
		Assert.assertEquals("Display.java:4746", secondFrame.location());
		Assert.assertNotEquals("", secondFrame.toString());
		JavaThread thread = threads.stream()
				.filter(t -> t.name().equals("Worker-916: Updating Git status for repository itest")).findFirst().get();
		Assert.assertEquals("org.eclipse.jgit.dircache.DirCacheIterator.<init>", thread.frames().get(12).method());
	}

	@Test(expected = IllegalArgumentException.class)
	public void invalidInput() {
		String data = "\"bleh\" #1";
		JstackParser.parseThreads(new StringReader(data)).findAny();
	}

	@Test
	public void concurrentParsing() {
		String data = Utils.readClassResource(JstackParserTest.class, "eclipse.txt");
		List<JavaThread> expected = FastChunkSplitter.splitToChunks((Reader) new StringReader(data)).map(JstackParser::parseThread).flatMap(Optional::stream).toList();
		for (int i = 0; i < 100; i++) {
			assertSameThreads(expected, JstackParser.parseThreads(new StringReader(data)).parallel().toList());
		}
	}

	@Test
	public void concurrentSplitting() {
		String data = Utils.readClassResource(JstackParserTest.class, "eclipse.txt");
		List<String> expected = FastChunkSplitter.splitToChunks((Reader) new StringReader(data)).toList();
		for (int i = 0; i < 100; i++) {
			assertSameStrings(expected, parallel( FastChunkSplitter.splitToChunks((Reader) new StringReader(data))).map(j -> "a" + j).map(j -> j.substring(1)).parallel().toList());
		}
	}
	
	@Test
	public void concurrentSplittingAndParsing() {
		String data = Utils.readClassResource(JstackParserTest.class, "eclipse.txt");
		List<JavaThread> expected = FastChunkSplitter.splitToChunks((Reader) new StringReader(data)).map(JstackParser::parseThread).flatMap(Optional::stream).toList();
		for (int i = 0; i < 100; i++) {
			assertSameThreads(expected, parallel( FastChunkSplitter.splitToChunks((Reader) new StringReader(data))).map(JstackParser::parseThread).flatMap(Optional::stream).toList());
		}
	}
	
	
	private static <T> void assertEquals(Collection<T> expected, Collection<T> actual, BiPredicate<T, T> areEqual) {
		actual = new ArrayList<T>(actual);
		
		for (T thread : expected) {
			Assert.assertTrue("Expected:\n" + thread + "\n Found:\n"+actual, removeFirst(actual, t -> areEqual.test(t, thread)));
		}
		
		Assert.assertEquals(Collections.emptyList(), actual);
	}
	
	private static final void assertSameThreads(Collection<JavaThread> expected, Collection<JavaThread> actual) {
		assertEquals(expected, actual, JavaThread::equalByMethodName);
	}
	
	private static final void assertSameStrings(Collection<String> expected, Collection<String> actual) {
		assertEquals(expected, actual, Object::equals);
	}

	
	private static <T> boolean removeFirst(Collection<T> list, Predicate<T> predicate) {
		for (Iterator<T> i = list.iterator(); i.hasNext();) {
			if (predicate.test(i.next())) {
				i.remove();
				return true;
			}
		}
		return false;
	}
}
