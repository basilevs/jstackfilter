package org.basilevs.jstackfilter.process;

import static org.basilevs.jstackfilter.JstackParser.parseThreads;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.Writer;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collector;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.DistinctBy;
import org.basilevs.jstackfilter.JavaThread;

/** Updates a registry file given as argument with data supplied on standard input
 * Supply -Dfile.encoding=<encoding> VM argument to set standard input encoding
 **/
public class Update {
	private static final Collector<JavaThread, Collection<JavaThread>, Collection<JavaThread>> DISTINCT_BY_NAME = new DistinctBy<JavaThread>(
			(t1, t2) -> t1.equalByMethodName(t2));

	public static void main(String[] args) throws IOException {
		Path registryPath = Path.of(args[0]);
		Collection<JavaThread> data;

		try (Reader reader = Files.newBufferedReader(registryPath)) {
			data = Stream.concat(parseThreads(reader), parseThreads(new InputStreamReader(System.in))).sequential().collect(DISTINCT_BY_NAME);
		}
		try (Writer writer = Files.newBufferedWriter(registryPath)) {
			for (JavaThread thread : data) {
				try {
					writer.write(thread.toString());
					writer.write("\n\n");
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			}
		}
	}

}
