package org.basilevs.jstackfilter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.regex.Pattern;

public final class JavaThread {

	private final List<Frame> frames;
	private final String name;
	private final String state;
	private final String representation;
	private final long id;

	public JavaThread(String name, long id, String state, Collection<Frame> frames) {
		this(name, id, state, frames, buildRepresentation(name, state, frames));

	}

	public JavaThread(String name, long id, String state, Collection<Frame> frames, String representation) {
		this.name = Objects.requireNonNull(name);
		this.state = Objects.requireNonNull(state);
		this.frames = List.copyOf(frames);
		this.representation = representation;
		this.id = id;
	}

	public List<Frame> frames() {
		return frames;
	}

	public String name() {
		return name;
	}

	public String state() {
		return state;
	}

	@Override
	public String toString() {
		return representation;
	}

	public boolean equalByMethodName(JavaThread that) {
		Object[] thisList = frames().stream().map(Frame::method).map(JavaThread::stripVersions).toArray();
		Object[] thatList = that.frames().stream().map(Frame::method).map(JavaThread::stripVersions).toArray();
		boolean result = Arrays.equals(thisList, thatList);
		return result;
	}

	public JavaThread withRepresentation(String representation) {
		return new JavaThread(this.name(), this.id(), this.state(), frames, representation);
	}

	private static final Pattern LAMBDA_PATTERN = Pattern.compile("\\$\\$Lambda\\$\\d+[\\./]0x[\\dabcdef]+\\.");

	private static String stripVersions(String input) {
		// org.eclipse.ui.internal.Workbench$$Lambda$189/0x0000000800dbfa08.run
		// org.eclipse.ui.infernal.Workbench$$Lambda$175/0x00000001003b2040.run
		//
		// Should produce same result
		return LAMBDA_PATTERN.matcher(input).replaceAll(JavaThread.class.getName());
	}

	private static String buildRepresentation(String name, String state, Collection<Frame> frames) {
		StringBuilder result = new StringBuilder();
		result.append(String.format("\"%s\"\n   java.lang.Thread.State: %s\n", name, state));
		for (Frame frame : frames) {
			result.append('\t').append(frame).append('\n');
		}
		return result.toString();
	}

	public long id() {
		return id;
	}
}
