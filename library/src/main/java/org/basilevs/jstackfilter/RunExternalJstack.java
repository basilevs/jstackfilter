package org.basilevs.jstackfilter;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;

public class RunExternalJstack {
	public static void main(String[] args) throws IOException {
		var pb = new ProcessBuilder();
		pb.command("jstack", args[0]);
		pb.redirectError(Redirect.INHERIT);
		Process process = pb.start();
		try {
			try (Reader reader = new InputStreamReader(process.getInputStream(), StandardCharsets.UTF_8)) {
				ProcessInput.process(reader);
			}
		} finally {
			process.destroy();
		}
	}

}
