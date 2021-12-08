package org.basilevs.jstackfilter;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Objects;

public final class JavaThread {

	private final List<Frame> frames;
	private final String name;
	private final String state;
	private final String representation;

	public JavaThread(String name, String state, Collection<Frame> frames, String representation) {
		this.name = Objects.requireNonNull(name);
		this.state = Objects.requireNonNull(state);
		this.frames = List.copyOf(frames);
		this.representation = representation;
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
//		StringBuilder result = new StringBuilder();
//		result.append(String.format("\"%s\"\n   java.lang.Thread.State: %s\n", name, state));
//		for (Frame frame : frames) {
//			result.append('\t').append(frame).append('\n');
//		}
		return representation;
	}

	
	public boolean equalByMethodName(JavaThread that) {
		Object[] thisList = frames().stream().map(Frame::method).toArray();
		Object[] thatList = that.frames().stream().map(Frame::method).toArray();
		return Arrays.equals(thisList, thatList);
	}
	
	public JavaThread withRepresentation(String representation) {
		return new JavaThread(this.name(), this.state(), frames, representation);
	}

}
