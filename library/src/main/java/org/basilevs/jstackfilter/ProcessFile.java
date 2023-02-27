package org.basilevs.jstackfilter;

import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;

public class ProcessFile {

	public static void main(String[] args) throws IOException {
		if (args.length < 1) {
			throw new IllegalArgumentException("Provide a file name as an argument");
		}
		try (Reader reader = new FileReader(args[0], StandardCharsets.UTF_8)) {
			ProcessInput.process(reader);
		}
	}

}
