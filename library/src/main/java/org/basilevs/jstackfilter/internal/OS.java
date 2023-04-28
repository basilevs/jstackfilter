package org.basilevs.jstackfilter.internal;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;

public enum OS {
	MACOS {
		@Override
		public Path configurationDirectory() throws FileNotFoundException {
			return checkWritable(Path.of(System.getProperty("user.home"), "Library", "Application Support", "org.basilevs.jstackfilter"));
		}
	},
	WINDOWS {
		@Override
		public Path configurationDirectory() throws FileNotFoundException {
			return checkWritable(Path.of(System.getenv("APPDATA"), "basilevs", "jstackfilter"));
		}
	},
	LINUX {
		@Override
		public Path configurationDirectory() throws FileNotFoundException {
			return checkWritable(Path.of(System.getProperty("user.home"), ".config", "jstackfilter"));
		}
	};

	public static OS parseOsName(String name) {
		if (name.startsWith("Mac OS X")) {
			return MACOS;
		} else if (name.startsWith("Windows")) {
			return WINDOWS;
		} else if (name.startsWith("Linux")) {
			return LINUX;
		} else {
			throw new IllegalArgumentException("Unknown OS: " + name);
		}
	}

	public static OS detect() {
		return parseOsName(System.getProperty("os.name"));
	}

	public abstract Path configurationDirectory() throws FileNotFoundException; 
	
	private static Path getExistingParent(Path path) {
		for (Path parent = path; parent != null; parent = parent.getParent()) {
			if (Files.exists(parent)) {
				return parent;
			}
		}
		throw new AssertionError("No parent for " + path);
	}
	
	private static Path checkWritable(Path file) throws FileNotFoundException {
		Path parent = getExistingParent(file);
		if (! Files.isDirectory(parent) || ! Files.isWritable(parent)) {
			throw new FileNotFoundException("Cannot write to " + file);
		}
		return file;
		
	}
}
