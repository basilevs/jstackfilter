package org.basilevs.jstackfilter.test;

import java.util.Collection;
import java.util.List;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.JstackParser;
import org.junit.Test;
import org.junit.Assert;

public class JavaThreadTest {

	@Test
	public void representationCanBeParsed() {
		Collection<Frame> frames = List.of(new Frame("package.class", "method"));
		JavaThread subject = new JavaThread("name", 7, "state", frames);
		JavaThread parsed = JstackParser.parseThread(subject.toString()).get();
		Assert.assertTrue(subject.equalByMethodName(parsed));
		Assert.assertTrue(parsed.equalByMethodName(subject));
		Assert.assertEquals(subject.id(), parsed.id());
	}

}
