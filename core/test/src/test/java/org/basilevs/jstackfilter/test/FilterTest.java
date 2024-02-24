package org.basilevs.jstackfilter.test;

import java.io.IOException;
import java.io.StringReader;

import org.basilevs.jstackfilter.Filter;
import org.junit.Test;

public class FilterTest {

	@Test
	public void doNotThrowOnNormalInput() throws IOException {
		var reader = new StringReader(Utils.readClassResource(FilterTest.class, "eclipse.txt"));
		Filter.process(reader);
	}

}
