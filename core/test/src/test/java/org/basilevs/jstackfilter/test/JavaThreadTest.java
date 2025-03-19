package org.basilevs.jstackfilter.test;

import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.util.Collection;
import java.util.List;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.JstackParser;
import org.junit.Test;

public class JavaThreadTest {

	@Test
	public void representationCanBeParsed() {
		Collection<Frame> frames = List.of(new Frame("package.class", "method"));
		JavaThread subject = new JavaThread("name", 7, "state", frames);
		JavaThread parsed = JstackParser.parseThread(subject.toString()).get();
		assertTrue(subject.equalByMethodName(parsed));
		assertTrue(parsed.equalByMethodName(subject));
		assertEquals(subject.id(), parsed.id());
	}
	
	@Test
	public void doNotBreakOnExtraQuotesInName() {
		Collection<Frame> frames = List.of(new Frame("package.class", "method"));
		JavaThread subject = new JavaThread("name with \"quotes\"", 7, "state", frames);
		JavaThread parsed = JstackParser.parseThread(subject.toString()).get();
		assertTrue(parsed.name().contains("name with"));
	}
	
	@Test
	public void matchJavaIndexing() {
		// org.eclipse.jdt.internal.core.search.processing.JobManager$$Lambda/0x000007ff017facf8.run
		// org.eclipse.jdt.internal.core.search.processing.JobManager$$Lambda/0x000018000168a2d8.run

		JavaThread subject1 = new JavaThread("1", 1, "state1", singletonList(new Frame("org.eclipse.jdt.internal.core.search.processing.JobManager$$Lambda/0x000007ff017facf8.run", "1")));
		JavaThread subject2 = new JavaThread("2", 2, "state2", singletonList(new Frame("org.eclipse.jdt.internal.core.search.processing.JobManager$$Lambda/0x000018000168a2d8.run", "2")));
		
		assertTrue(subject1.equalByMethodName(subject2));
		assertTrue(subject2.equalByMethodName(subject1));
	}

}
