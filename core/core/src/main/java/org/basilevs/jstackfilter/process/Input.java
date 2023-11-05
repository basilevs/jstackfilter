package org.basilevs.jstackfilter.process;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

import org.basilevs.jstackfilter.Filter;

public class Input {

	public static void main(String[] args) throws IOException {
		try (Reader reader = new InputStreamReader(System.in, StandardCharsets.UTF_8)) {
			Filter.process(reader);
		}
	}


}
