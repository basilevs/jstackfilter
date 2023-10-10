package org.basilevs.jstackfilter;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

/**
 * A Reader that concatenates a Stream of CharSequences into a single continuous
 * character stream. This class is designed to provide a bridge between
 * Stream-based and Reader-based APIs.
 *
 * <p>This class takes responsibility for closing the Stream it's wrapping.
 * The {@link #close()} method will close the underlying Stream.
 *
 * <p>This class is not thread-safe. If multiple threads need to read from the
 * same StreamReader, they should synchronize their access to it externally.
 *
 * <p>This class does not support marking and resetting. The {@link #mark(int)}
 * and {@link #reset()} methods from the Reader class are not overridden and
 * will not work as some might expect.
 *
 * @see java.io.Reader
 * @see java.util.stream.Stream
 */
public class StreamReader extends Reader {
	private final Stream<? extends CharSequence> delegate;
	private final StringBuilder buffer = new StringBuilder();
	private final Iterator<? extends CharSequence> iterator;

	public StreamReader(Stream<? extends CharSequence> input) {
		this.delegate = Objects.requireNonNull(input);
		this.iterator = input.iterator();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		while (buffer.length() > 0 || iterator.hasNext()) {
			if (buffer.length() > 0) {
				int length = Math.min(buffer.length(), len);
				buffer.getChars(0, length, cbuf, off);
				buffer.delete(0, length);
				return length;
			}
			if (!iterator.hasNext()) {
				return -1;
			}
			buffer.append(iterator.next());
		}
		return -1;
	}

	@Override
	public void close() throws IOException {
		try {
			delegate.close();
		} catch (RuntimeException e) {
			var cause = e.getCause();
			if (cause instanceof IOException) {
				throw (IOException) cause;
			}
			throw e;
		}
	}

}
