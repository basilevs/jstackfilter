package org.basilevs.jstackfilter;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class Filter {
	private Filter() {
	}

	public static void process(Reader reader) throws IOException {
		copy(filter(reader), new OutputStreamWriter(System.out));
	}

	public static Reader filter(Reader reader) {
		Stream<JavaThread> stacks = JstackParser.parseThreads(reader);
		Stream<JavaThread> stacksCopy = stacks;
		stacksCopy = stacksCopy.filter(Predicate.not(Known::isKnown));
		stacksCopy = stacksCopy.collect(new DistinctBy<JavaThread>((t1, t2) -> t1.equalByMethodName(t2))).stream();
		stacksCopy.onClose(stacks::close);
		return new StreamReader(stacksCopy.map(thread -> thread + "\n\n"));
	}

	public static void copy(Reader input, Writer output) throws IOException {
		char[] buffer = new char[1024 * 1024];
		int n = 0;
		while (-1 != (n = input.read(buffer))) {
			output.write(buffer, 0, n);
		}
	}

}
