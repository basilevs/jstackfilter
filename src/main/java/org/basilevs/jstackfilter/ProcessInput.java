package org.basilevs.jstackfilter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class ProcessInput {

	public static void main(String[] args) throws IOException {
		try (Reader reader = new InputStreamReader(System.in, StandardCharsets.UTF_8)) {
			RunExternalJstack.process(reader);
		}
	}

}
