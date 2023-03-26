package org.basilevs.jstackfilter;

import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Arrays;

public final class ExternalJstack {

	public static Path findExternalJstack() {
		return findExecutableOnPath("jstack");
	}
	
	public static Path findExecutableOnPath(String name) {
		String[] extensions = new String[] {"", ".exe", ".cmd", ".bat"};
		ArrayList<Path> paths = new ArrayList<>();
		var home = System.getProperties().getProperty("java.home", null);
		if (home != null) {
			paths.add(Path.of(home, "bin"));
		}
		home = System.getenv("JAVA_HOME");
		if (home != null) {
			paths.add(Path.of(home, "bin"));
		}
		Arrays.stream(System.getenv("PATH").split(File.pathSeparator)).<Path>map(Path::of).forEach(paths::add);
		
	    for (Path directory: paths) {
	    	for (String extension: extensions) {
		        Path file = directory.resolve(name+extension);
		        if (Files.isExecutable(file)) {
		            return file.toAbsolutePath();
		        }
	    	}
	    }
	    throw new IllegalStateException("Can't find jstack program");
	}
	
	public static Reader read(long pid) throws IOException {
		var pb = new ProcessBuilder();
		pb.command(org.basilevs.jstackfilter.ExternalJstack.findExternalJstack().toString(), "" + pid);
		pb.redirectError(Redirect.INHERIT);
		Process process = pb.start();
		try {
			return new InputStreamReader(Filter.onClose(process.getInputStream(), process::destroy), StandardCharsets.UTF_8);
		} catch(Throwable e) {
			process.destroy();
			throw e;
		}
	}

}
