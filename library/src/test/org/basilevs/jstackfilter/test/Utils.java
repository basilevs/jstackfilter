package org.basilevs.jstackfilter.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;

public class Utils {
	public static String readFromInputStream(InputStream inputStream) throws IOException {
		StringBuilder resultStringBuilder = new StringBuilder();
		char[] buffer = new char[100 * 1024];
		try (BufferedReader br = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))) {
			for (int length = br.read(buffer); length >= 0; length = br.read(buffer)) {
				resultStringBuilder.append(buffer, 0, length);
			}
		} finally {
			inputStream.close();
		}
		return resultStringBuilder.toString();
	}
}
