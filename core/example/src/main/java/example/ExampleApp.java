package example;

import java.io.StringReader;
import java.util.List;

import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.JstackParser;

public final class ExampleApp {
	private ExampleApp() {
	}

	public static void main(String[] args) {
		String input = "\"main\" #1 prio=5 os_prio=0 cpu=40.13ms elapsed=10.27s tid=0x0000000000000001 nid=0x1 runnable [0x0000000000000001]\n"
				+ "   java.lang.Thread.State: RUNNABLE\n"
				+ "\tat java.lang.Thread.sleep(java.base@17.0.10/Native Method)\n"
				+ "\tat demo.App.run(App.java:10)\n\n"
				+ "\"Reference Handler\" #2 daemon prio=10 os_prio=0 cpu=1.35ms elapsed=10.27s tid=0x0000000000000002 nid=0x2 waiting on condition [0x0000000000000002]\n"
				+ "   java.lang.Thread.State: RUNNABLE\n"
				+ "\tat java.lang.ref.Reference.waitForReferencePendingList(java.base@17.0.10/Native Method)\n"
				+ "\tat java.lang.ref.Reference.processPendingReferences(java.base@17.0.10/Reference.java:253)\n\n";

		List<JavaThread> threads;
		try (StringReader reader = new StringReader(input);
				var stream = JstackParser.parseThreads(reader)) {
			threads = stream.toList();
		}

		System.out.println("Parsed threads: " + threads.size());
		for (JavaThread thread : threads) {
			System.out.println("- #" + thread.id() + " " + thread.name() + " [" + thread.state() + "]");
		}
	}
}
