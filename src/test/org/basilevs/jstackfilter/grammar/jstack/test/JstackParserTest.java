package org.basilevs.jstackfilter.grammar.jstack.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.Collection;
import java.util.function.Predicate;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.grammar.jstack.JstackParser;
import org.basilevs.jstackfilter.grammar.jstack.ParseException;
import org.junit.Assert;
import org.junit.Test;

public class JstackParserTest {

	@Test
	public void frame1() throws IOException, ParseException {
		String data = Utils.readFromInputStream(JstackParserTest.class.getResourceAsStream("eclipse.txt")); 
		JstackParser subject = new JstackParser(new StringReader(data));
		Collection<JavaThread> threads = subject.jstackDump();
		Assert.assertTrue(threads.stream().map(JavaThread::name).anyMatch(Predicate.isEqual("Active Thread: Equinox Container: 4201e9ce-3eab-4ab0-8431-f4be70328a4d")));
		JavaThread first = threads.iterator().next();
		Frame firstFrame = first.frames().get(0);
		Assert.assertEquals(firstFrame.method(), "org.eclipse.swt.internal.win32.OS.WaitMessage");		
		Assert.assertEquals(firstFrame.location(), "Native Method");
		Frame secondFrame = first.frames().get(1);
		Assert.assertEquals(secondFrame.method(), "org.eclipse.swt.widgets.Display.sleep");		
		Assert.assertEquals(secondFrame.location(), "Display.java:4750");
	}

}
