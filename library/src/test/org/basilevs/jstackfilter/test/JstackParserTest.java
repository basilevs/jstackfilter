package org.basilevs.jstackfilter.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.JstackParser;
import org.junit.Assert;
import org.junit.Test;

public class JstackParserTest {

	@Test
	public void frame1() throws IOException {
		String data = Utils.readFromInputStream(JstackParserTest.class.getResourceAsStream("eclipse.txt")); 
		Collection<JavaThread> threads = JstackParser.parseThreads(new StringReader(data)).collect(Collectors.toList());
		Assert.assertTrue(threads.stream().map(JavaThread::name).anyMatch(Predicate.isEqual("Active Thread: Equinox Container: 4201e9ce-3eab-4ab0-8431-f4be70328a4d")));
		JavaThread first = threads.iterator().next();
		Frame firstFrame = first.frames().get(0);
		Assert.assertEquals(firstFrame.method(), "org.eclipse.swt.internal.win32.OS.WaitMessage");		
		Assert.assertEquals(firstFrame.location(), "Native Method");
		Frame secondFrame = first.frames().get(1);
		Assert.assertEquals(secondFrame.method(), "org.eclipse.swt.widgets.Display.sleep");		
		Assert.assertEquals("Display.java:4746", secondFrame.location());
		Assert.assertNotEquals("", secondFrame.toString());
		JavaThread thread = threads.stream().filter(t -> t.name().equals("Worker-916: Updating Git status for repository itest")).findFirst().get();
		Assert.assertEquals("org.eclipse.jgit.dircache.DirCacheIterator.<init>", thread.frames().get(12).method());
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void invalidInput() {
		String data = "\"bleh\" #1";
		JstackParser.parseThreads(new StringReader(data)).findAny();
	}

}
