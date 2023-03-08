package org.basilevs.jstackfilter.ui.internal;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.lang.ProcessBuilder.Redirect;
import java.util.Scanner;

public class SystemUtil {

	public static InputStream captureOutput(String... commandLine) throws IOException {
		var pb = new ProcessBuilder();
		pb.command(commandLine);
		pb.redirectError(Redirect.INHERIT);
		Process process = pb.start();
		return SystemUtil.onClose(process.getInputStream(), process::destroy);
	}

	public static InputStream onClose(InputStream delegate, Runnable runnable) {
		return new FilterInputStream(delegate) {
			@Override
			public void close() throws IOException {
				super.close();
				runnable.run();
			}
		};
	}

	public static String toString(Reader input) {
		try (var scanner = new Scanner(input)) {
			scanner.useDelimiter("\\A");
			if (!scanner.hasNext()) {
				return "";
			}
			return scanner.next();
		}
	}

}
