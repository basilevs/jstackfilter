package org.basilevs.jstackfilter.ui;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;

import org.basilevs.jstackfilter.ProcessInput;
import org.basilevs.jstackfilter.ui.internal.SystemUtil;

public class Model {
	private final ExecutorService executor = Executors.newFixedThreadPool(1);
	private static final long CURRENT_PID = ProcessHandle.current().pid();
	private final Consumer<String> errorListener;
	private final Consumer<String> outputListener;
	private boolean filter = true;
	private long pid = 0;

	public Model(Consumer<String> errorListener, Consumer<String> outputListener) {
		super();
		this.outputListener = Objects.requireNonNull(outputListener);
		this.errorListener = Objects.requireNonNull(errorListener);
	}

	public void selectRow(Object firstColumn) {
		this.pid = (Long) firstColumn;
		update();
	}

	private void update() {
		try {
			outputListener.accept(SystemUtil.toString(jstackByPid(pid)));
		} catch (Exception e) {
			handleError(e);
		}
	}
	
	public void setFilter(boolean filter) {
		this.filter = filter;
		update();
	}

	public void close() {
		executor.shutdownNow();
	}

	public List<JavaProcess> getJavaProcesses() {
		ArrayList<JavaProcess> rows = new ArrayList<>();
		try (Scanner lines = new Scanner(SystemUtil.captureOutput(executor, "jps", "-v"), StandardCharsets.UTF_8)) {
			try {
				lines.useDelimiter("\n");
				while (lines.hasNext()) {
					Scanner fields = new Scanner(lines.next());
					fields.useDelimiter("\s+");
					if (fields.hasNext()) {
						long pid = Long.parseLong(fields.next());
						fields.skip("\s+");
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
		Reader result = new InputStreamReader(SystemUtil.captureOutput(executor, "jstack", "" + pid), StandardCharsets.UTF_8);
		if (filter) {
			return ProcessInput.filter(result);
		} else {
			return result;
		}
	}

}
