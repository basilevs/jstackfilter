package org.basilevs.jstackfilter.test;

import static org.junit.Assert.assertTrue;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.JstackParser;
import org.junit.Test;

public class FrameTest {

	@Test
	public void ignoreLambdaID() {
		Frame a = create("java.lang.invoke.LambdaForm$MH/0x00003f8001006800.invoke(java.base@23.0.1/LambdaForm$MH)");
		Frame b = create("java.lang.invoke.LambdaForm$MH/0x0000180001006800.invoke(java.base@23.0.2/LambdaForm$MH)");
		assertTrue(a.equalByMethodName(b));
	}
		
	@Test
	public void ignoreLambdaName() {
		Frame a = create("org.eclipse.ui.internal.Workbench$$Lambda$183/0x0000000100399440.run(Unknown Source)");
		Frame b = create("org.eclipse.ui.internal.Workbench$$Lambda$227/0x0000000800e87928.run(Unknown Source)");
		assertTrue(a.equalByMethodName(b));
	}
	
	private static Frame create(String line) {
		String paragraph = String.format("\"main\" #1 [259] prio=6 os_prio=31 cpu=42930.74ms elapsed=19201.01s tid=0x000000014300b200 nid=259 runnable  [0x000000016bcee000]\n   java.lang.Thread.State: RUNNABLE\n"
				+ "	at %s", line);
		JavaThread thread = JstackParser.parseThread(paragraph).orElseThrow();
		return thread.frames().get(0);
		}

}
