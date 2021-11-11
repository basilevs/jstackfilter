package org.basilevs.jstackfilter;

import java.io.IOException;
import java.io.Reader;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.grammar.jstack.ParseException;

public class JstackParser {

	public static Stream<JavaThread> parse(Reader reader) throws IOException {
		var parser = new org.basilevs.jstackfilter.grammar.jstack.JstackParser(reader);
		try {
			return parser.jstackDump().stream();
		} catch (ParseException e) {
			throw new IOException(e);
		}
	}

}
