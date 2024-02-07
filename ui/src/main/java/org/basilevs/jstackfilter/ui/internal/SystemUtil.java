package org.basilevs.jstackfilter.ui.internal;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;

import org.basilevs.jstackfilter.Filter;

public class SystemUtil {

	public static final class ErrorOutput extends IOException {
		private static final long serialVersionUID = 4137648395124930959L;

		public ErrorOutput(String output) {
			super(output);
		}
	}

	public static InputStream captureOutput(Executor executor, String... commandLine) throws IOException {
		var pb = new ProcessBuilder();
		pb.command(commandLine);
		Process process = pb.start();
		var error = CompletableFuture.supplyAsync(() -> {
			try (var stream = process.getErrorStream()) {
				return toString(new InputStreamReader(stream, StandardCharsets.UTF_8));
			} catch (IOException e) {
				throw new IllegalStateException(e);
			}
		}, executor);
		return Filter.onClose(process.getInputStream(), () -> {
			process.destroy();
			try {
				if (!error.get().trim().isEmpty()) {
					throw new ErrorOutput(error.get());
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				throw new IOException("Interrupted while waiting for error stream of " + commandLine);
			} catch (ExecutionException e) {
				var ioException = findIoException(e);
				if (ioException != null) {
					throw ioException;
				}
				throw new IOException(e.getCause());
			}
		});
	}

	private static IOException findIoException(Exception e) {
		if (e == null) {
			return null;
		}
		if (e instanceof IOException) {
			return (IOException) e;
		}
		var cause = e.getCause();
		if (cause instanceof Exception) {
			return findIoException((Exception) cause);
		}
		return null;
	}

	public static String toString(Reader input) throws IOException {
		try (var scanner = new Scanner(input)) {
			try {
				scanner.useDelimiter("\\A");
				if (!scanner.hasNext()) {
					return "";
				}
				return scanner.next();
			} finally {
				scanner.close();
				var error = scanner.ioException();
				if (error != null) {
					throw error;
				}
			}
		}
	}

}
