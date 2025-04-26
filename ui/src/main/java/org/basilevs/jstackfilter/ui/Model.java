package org.basilevs.jstackfilter.ui;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.function.Predicate;

import org.basilevs.jstackfilter.Filter;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.ThreadRegistry;
import org.basilevs.jstackfilter.ui.internal.SystemUtil;

public class Model {
	private interface ReaderSupplier {
		Reader read() throws IOException;
	}

	private static final ReaderSupplier NO_INPUT = () -> new StringReader("No input selected");
	private final ExecutorService executor = Executors.newFixedThreadPool(1);
	private static final long CURRENT_PID = ProcessHandle.current().pid();
	private final Consumer<String> errorListener;
	private final Consumer<String> outputListener;
	private boolean showIdle = true;
	private ReaderSupplier input = NO_INPUT;
	private final ThreadRegistry idle;
	private final Set<Long> oldProcesses = new HashSet<>();
	private long selectedPid = -1;
	{
		try {
			idle = ThreadRegistry.idle();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	public Model(Consumer<String> errorListener, Consumer<String> outputListener) {
		super();
		this.outputListener = Objects.requireNonNull(outputListener);
		this.errorListener = Objects.requireNonNull(errorListener);
	}

	public void selectJavaProcess(long pid) {
		if (pid == selectedPid) {
			return;
		}
		selectedPid = pid;
		input = () -> jstackByPid(pid);
		update();
	}

	public void setFile(Path path) {
		input = () -> loadFile(path);
		update();
	}

	public void setInput(String jstackOutput) {
		this.input = () -> new StringReader(jstackOutput);
		update();
	}

	private void update() {
		try {
			filter(input.read());
		} catch (Exception e) {
			handleError(e);
		}
	}

	public void showIdle(boolean doShow) {
		this.showIdle = doShow;
		update();
	}

	public void showOldProcesses(boolean selected) {
		oldProcesses.clear();
		if (!selected) {
			getJavaProcesses().stream().map(JavaProcess::pid).forEach(oldProcesses::add);
		}
	}

	private void filter(Reader input) throws IOException {
		try {
			Reader filtered;
			if (!showIdle) {
				filtered = Filter.filter(Predicate.<JavaThread>not(idle::contains), input);
			} else {
				filtered = input;
			}
			var str = SystemUtil.toString(filtered);
			if (str.isEmpty()) {
				str = "No interesting threads";
			}
			outputListener.accept(str);
		} finally {
			input.close();
		}
	}
	
	public void rememberIdleThreads(String selection) {
		try {
			idle.load(new StringReader(selection));
			update();
		} catch (Exception e) {
			handleError(e);
		}
	}

	public void close() throws IOException {
		executor.shutdownNow();
		idle.close();
	}
	public List<JavaProcess> getJavaProcesses() {
		List<JavaProcess> result = parseJavaProcesses();
		result.removeIf(p -> oldProcesses.contains(p.pid()));
		return result;
	}

	public List<JavaProcess> parseJavaProcesses() {
		ArrayList<JavaProcess> rows = new ArrayList<>();
		try (Scanner lines = new Scanner(SystemUtil.captureOutput(executor, "jps", "-v"), StandardCharsets.UTF_8)) {
			try {
				lines.useDelimiter("\n");
				while (lines.hasNext()) {
					try (Scanner fields = new Scanner(lines.next())) {
						fields.useDelimiter("\\s+");
						if (fields.hasNext()) {
							long pid = Long.parseLong(fields.next());
							fields.skip("\\s+");
							fields.useDelimiter("\\A");
							var rest = fields.next();
							if (rest.startsWith("Jps")) {
								continue;
							}
							if (pid == CURRENT_PID) {
								continue;
							}
							rows.add(new JavaProcess(pid, rest));
						}
					}
				}
				lines.close();
			} finally {
				var error = lines.ioException();
				if (error != null) {
					throw error;
				}
			}
		} catch (Exception e) {
			handleError(e);
		}
		return rows;
	}

	private void handleError(Exception e) {
		if (e instanceof SystemUtil.ErrorOutput) {
			errorListener.accept(e.getMessage());
		} else {
			e.printStackTrace();
			var text = new StringWriter();
			e.printStackTrace(new PrintWriter(text));
			errorListener.accept(text.toString());
		}
	}

	private Reader jstackByPid(long pid) throws IOException {
		return new InputStreamReader(SystemUtil.captureOutput(executor, "jstack", "" + pid), StandardCharsets.UTF_8);
	}

	private Reader loadFile(Path file) throws IOException {
		return Files.newBufferedReader(file, StandardCharsets.UTF_8);
	}

}
