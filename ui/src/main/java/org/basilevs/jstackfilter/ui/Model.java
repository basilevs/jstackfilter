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
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.DistinctBy;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.JstackParser;
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
	private long lastJavaProcess = 0;
	private boolean showIdentical = false;
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
		lastJavaProcess = pid;
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
		try (Reader reader = input.read()) { 
			filter(reader);
		} catch (Exception e) {
			handleError(e);
		}
	}

	public void showIdle(boolean doShow) {
		this.showIdle = doShow;
		update();
	}
	
	public void showIdentical(boolean doShow) {
		this.showIdentical = doShow;
		update();
	}

	public void showOldProcesses(boolean selected) {
		oldProcesses.clear();
		if (!selected) {
			getJavaProcesses().stream().map(JavaProcess::pid).filter(pid -> pid != lastJavaProcess).forEach(oldProcesses::add);
		}
	}

	private void filter(Reader input) throws IOException {
		String result;
		try (Stream<JavaThread> stacks = JstackParser.parseThreads(input)) {
			Stream<JavaThread> stacksCopy = stacks;
			if (showIdentical && showIdle) {
				result =  SystemUtil.toString(input);;
			} else {
				if (!showIdentical) {
					stacksCopy = stacksCopy.collect(new DistinctBy<JavaThread>((t1, t2) -> t1.equalByMethodName(t2))).stream();
				}
				if (!showIdle) {
					stacksCopy = stacksCopy.filter(Predicate.<JavaThread>not(idle::contains));
				}
				result = stacksCopy.map(Object::toString).collect(Collectors.joining("\n\n"));
			}
		}
		if (result.isEmpty()) {
			result = "No interesting threads";
		}
		outputListener.accept(result);
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
