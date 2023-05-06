package org.basilevs.jstackfilter;

import java.io.Closeable;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.Reader;
import java.io.Writer;
import java.util.function.Predicate;
import java.util.stream.Stream;

public final class Filter {
	private Filter() {
	}

	public static void process(Reader reader) throws IOException {
		try (Known known = new Known()) {
			copy(filter(Predicate.<JavaThread>not(known::isKnown), reader), new OutputStreamWriter(System.out));
		}
	}

	public static Reader filter(Predicate<JavaThread> select, Reader reader) {
		Stream<JavaThread> stacks = JstackParser.parseThreads(reader);
		Stream<JavaThread> stacksCopy = filter(select, stacks);
		return new StreamReader(stacksCopy.map(thread -> thread + "\n\n"));
	}

	public static Stream<JavaThread> filter(Predicate<JavaThread> select, Stream<JavaThread> stacks) {
		Stream<JavaThread> stacksCopy = stacks;
		stacksCopy = stacksCopy.filter(select);
		stacksCopy = stacksCopy.collect(new DistinctBy<JavaThread>((t1, t2) -> t1.equalByMethodName(t2))).stream();
		stacksCopy.onClose(stacks::close);
		return stacksCopy;
	}

	public static InputStream onClose(InputStream delegate, Closeable runnable) {
		return new FilterInputStream(delegate) {
			@Override
			public void close() throws IOException {
				try {
					super.close();
				} finally {
					runnable.close();
				}
			}
		};
	}

	public static void copy(Reader input, Writer output) throws IOException {
		try {
			char[] buffer = new char[1024 * 1024];
			int n = 0;
			while (-1 != (n = input.read(buffer))) {
				output.write(buffer, 0, n);
			}
		} finally {
			try {
				input.close();
			} finally {
				output.close();
			}
		}
	}

}
