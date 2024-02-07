package org.basilevs.jstackfilter.ui;

import java.util.Objects;

public class JavaProcess {
	public final long pid;
	public final String command;

	public JavaProcess(long pid, String command) {
		this.pid = pid;
		this.command = Objects.requireNonNull(command);
	}

	public String command() {
		return command;
	}

	public long pid() {
		return pid;
	}

}
