package org.basilevs.jstackfilter;

import java.util.Objects;
import java.util.regex.Pattern;

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
	
	public boolean equalByMethodName(Frame that) {
		if (that == null) {
			return false;
		}
		return Objects.equals(stripVersions(method), stripVersions(that.method));
	}
	
	// org.eclipse.ui.internal.Workbench$$Lambda$189/0x0000000800dbfa08.run
	// org.eclipse.ui.infernal.Workbench$$Lambda$175/0x00000001003b2040.run
	//
	// org.eclipse.jdt.internal.core.search.processing.JobManager$$Lambda/0x000007ff017facf8.run
	// org.eclipse.jdt.internal.core.search.processing.JobManager$$Lambda/0x000018000168a2d8.run
	//
	// java.lang.invoke.LambdaForm$MH/0x00003f8001006800.invoke
	// java.lang.invoke.LambdaForm$MH/0x0000180001006800.invoke
	
	// Should produce same result
	private static final Pattern LAMBDA_PATTERN = Pattern.compile("(?:\\$|0x)[\\dabcdef]+");

	private static String stripVersions(String input) {
		String result = LAMBDA_PATTERN.matcher(input).replaceAll(JavaThread.class.getName());
		return result;
	}
}
