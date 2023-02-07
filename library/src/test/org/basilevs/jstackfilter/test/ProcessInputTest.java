package org.basilevs.jstackfilter.test;

import static org.basilevs.jstackfilter.ProcessInput.process;

import java.io.IOException;
import java.io.InputStreamReader;

import org.junit.Test;

public class ProcessInputTest {

	@Test
	public void doNotThrowOnNormalInput() throws IOException {
		try (var data = JstackParserTest.class.getResourceAsStream("eclipse.txt");
				var reader = new InputStreamReader(data)) {
			process(reader);
		}
	}
	
}
