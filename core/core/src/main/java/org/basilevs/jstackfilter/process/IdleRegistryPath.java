package org.basilevs.jstackfilter.process;

import java.io.IOException;

import org.basilevs.jstackfilter.ThreadRegistry;

/** Prints the user-specific idle registry path. */
public class IdleRegistryPath {

	public static void main(String[] args) throws IOException {
		System.out.println(ThreadRegistry.idle().configurationFile());
	}
}
