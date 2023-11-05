package org.basilevs.jstackfilter;

import java.util.Objects;

public final class Frame {
	private final String location;
	private final String method;

	public Frame(String method, String location) {
		super();
		this.method = Objects.requireNonNull(method);
		this.location = Objects.requireNonNull(location);
	}

	@Override
	public String toString() {
		return "at " + method + "(" + location + ")";
	}

	public String method() {
		return method;
	}

	public String location() {
		return location;
	}
	
	
}
