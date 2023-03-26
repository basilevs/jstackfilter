package org.basilevs.jstackfilter.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.JstackParser;
import org.basilevs.jstackfilter.Known;
import org.junit.Assert;
import org.junit.Test;

public class KnownTest {

	@Test
	public void test() throws IOException {
		String data = Utils.readFromInputStream(JstackParserTest.class.getResourceAsStream("eclipse.txt"));
		Collection<JavaThread> threads = JstackParser.parseThreads(new StringReader(data)).collect(Collectors.toList());
		JavaThread first = threads.iterator().next();
		Assert.assertTrue(Known.isKnown(first));
		List<Frame> frames = new ArrayList<>(first.frames());
		JavaThread another = new JavaThread(first.name(), 0, first.state(), frames);
		Assert.assertTrue(Known.isKnown(another));
		another = new JavaThread("meh", 0, first.state(), frames);
		Assert.assertTrue(Known.isKnown(another));
		another = new JavaThread(first.name(), 0, "boo", frames);
		Assert.assertTrue(Known.isKnown(another));
		frames.remove(0);
		another = new JavaThread(first.name(), 0, first.state(), frames);
		Assert.assertFalse(Known.isKnown(another));
	}
	
	@Test
	public void knownThreadsAreUnique() {
		List<JavaThread> subject = new ArrayList<>(Known.threads);
		while (!subject.isEmpty()) {
			JavaThread first = subject.remove(0);
			for (JavaThread i : subject) {
				boolean matches = i.equalByMethodName(first);
				Assert.assertFalse("" + first +"\n" + i , matches);
			}
		}
	}

}
