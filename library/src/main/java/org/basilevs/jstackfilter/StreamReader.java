package org.basilevs.jstackfilter;

import java.io.IOException;
import java.io.Reader;
import java.util.Iterator;
import java.util.Objects;
import java.util.stream.Stream;

public class StreamReader extends Reader {
	private final Stream<? extends CharSequence> delegate;
	private final StringBuilder  buffer = new StringBuilder();
	private final Iterator<? extends CharSequence> iterator;
	
	public StreamReader(Stream<? extends CharSequence> input) {
		this.delegate = Objects.requireNonNull(input);
		this.iterator = input.iterator();
	}

	@Override
	public int read(char[] cbuf, int off, int len) throws IOException {
		while (buffer.length() > 0 || iterator.hasNext() ) {
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
		delegate.close();
	}

}
