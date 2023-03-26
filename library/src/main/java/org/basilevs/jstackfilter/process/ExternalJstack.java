package org.basilevs.jstackfilter.process;

import java.io.IOException;
import java.io.Reader;

import org.basilevs.jstackfilter.Filter;

public class ExternalJstack {
	public static void main(String[] args) throws IOException {
		try (Reader reader = org.basilevs.jstackfilter.ExternalJstack.read(Long.valueOf(args[0]))) {
			Filter.process(reader);
		}
	}

}
