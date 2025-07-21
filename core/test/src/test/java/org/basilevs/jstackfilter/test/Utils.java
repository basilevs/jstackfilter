package org.basilevs.jstackfilter.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.JstackParser;

class Utils {
	public static String inputStreamToString(InputStream inputStream) throws IOException {
		StringBuilder resultStringBuilder = new StringBuilder();
		char[] buffer = new char[100 * 1024];
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			for (int length = br.read(buffer); length >= 0; length = br.read(buffer)) {
				resultStringBuilder.append(buffer, 0, length);
			}
		} finally {
			if (inputStream != null)
				inputStream.close();
		}
		return resultStringBuilder.toString();
	}

	@SuppressWarnings("resource")
	public static String readClassResource(Class<?> clazz, String relativeResourcePath) {
		try {
			InputStream result = JstackParserTest.class.getResourceAsStream(relativeResourcePath);
			if (result != null)
				return inputStreamToString(result);
		} catch (IOException e) {
			throw new AssertionError(e);
		}
		throw new AssertionError("Can't load " + relativeResourcePath + " from " + clazz);
	}

	public static List<JavaThread> readThreadResource(Class<?> origin, String name) {
		try (Stream<JavaThread> threads = JstackParser.parseThreads( new StringReader(readClassResource(origin, name)))) {
			return threads.collect(Collectors.toList());
		}
	}
	
}
