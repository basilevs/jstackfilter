package org.basilevs.jstackfilter.grammar.jstack.test;

import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.Idle;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.grammar.jstack.JstackParser;
import org.basilevs.jstackfilter.grammar.jstack.ParseException;
import org.junit.Assert;
import org.junit.Test;

public class IdleTest {

	@Test
	public void test() throws IOException, ParseException {
		String data = Utils.readFromInputStream(JstackParserTest.class.getResourceAsStream("eclipse.txt")); 
		JstackParser subject = new JstackParser(new StringReader(data));
		Collection<JavaThread> threads = subject.jstackDump();
		JavaThread first = threads.iterator().next();
		Assert.assertTrue(Idle.isIdle(first));
		List<Frame> frames = new ArrayList<>(first.frames());
		JavaThread another = new JavaThread(first.name(), first.state(), frames);
		Assert.assertTrue(Idle.isIdle(another));
		another = new JavaThread("meh", first.state(), frames);
		Assert.assertTrue(Idle.isIdle(another));
		another = new JavaThread(first.name(), "boo", frames);
		Assert.assertTrue(Idle.isIdle(another));
		frames.remove(0);
		another = new JavaThread(first.name(), first.state(), frames);
		Assert.assertFalse(Idle.isIdle(another));
	}

}
