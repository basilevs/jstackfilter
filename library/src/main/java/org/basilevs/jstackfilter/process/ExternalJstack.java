package org.basilevs.jstackfilter.process;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;

import org.basilevs.jstackfilter.Filter;

public class ExternalJstack {
	public static void main(String[] args) throws IOException {
		var pb = new ProcessBuilder();
		pb.command("jstack", args[0]);
		pb.redirectError(Redirect.INHERIT);
		Process process = pb.start();
		try {
			try (Reader reader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)) {
				Filter.process(reader);
			}
		} finally {
			process.destroy();
		}
	}

}
