package org.basilevs.jstackfilter.test;

import java.io.IOException;
import java.io.InputStreamReader;

import org.basilevs.jstackfilter.Filter;
import org.junit.Test;

public class FilterTest {

	@Test
	public void doNotThrowOnNormalInput() throws IOException {
		try (var data = FilterTest.class.getResourceAsStream("eclipse.txt");
				var reader = new InputStreamReader(data)) {
			Filter.process(reader);
		}
	}
	
}
