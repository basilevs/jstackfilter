package org.basilevs.jstackfilter.grammar.jstack.test;

import java.io.IOException;
import java.io.StringReader;

import org.basilevs.jstackfilter.grammar.jstack.JstackParser;
import org.basilevs.jstackfilter.grammar.jstack.ParseException;
import org.junit.Test;

public class JstackParserTest {

	@Test
	public void frame1() throws IOException, ParseException {
		String data = Utils.readFromInputStream(JstackParserTest.class.getResourceAsStream("eclipse.txt")); 
		JstackParser subject = new JstackParser(new StringReader(data));
		subject.JstackDump();
	}

}
