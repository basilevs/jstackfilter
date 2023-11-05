package org.basilevs.jstackfilter.internal;

import java.io.FileNotFoundException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public enum OS {
	MACOS {
		@Override
		public Path configurationDirectory() throws FileNotFoundException {
			return ensureWritable(Paths.get(System.getProperty("user.home"), "Library", "Application Support", "org.basilevs.jstackfilter"));
		}
	},
	WINDOWS {
		@Override
		public Path configurationDirectory() throws FileNotFoundException {
			return ensureWritable(Paths.get(System.getenv("APPDATA"), "basilevs", "jstackfilter"));
		}
	},
	LINUX {
		@Override
		public Path configurationDirectory() throws FileNotFoundException {
			return ensureWritable(Paths.get(System.getProperty("user.home"), ".config", "jstackfilter"));
		}
	};

	public static OS detect() {
		String osName = System.getProperty("os.name").toLowerCase();
		if (osName.contains("mac")) {
			return MACOS;
		} else if (osName.contains("win")) {
			return WINDOWS;
		} else if (osName.contains("nix") || osName.contains("nux")) {
			return LINUX;
		} else {
			throw new IllegalArgumentException("Unsupported OS: " + osName);
		}
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
	
	private static Path ensureWritable(Path file) throws FileNotFoundException {
		Path parent = getExistingParent(file);
		if (! Files.isDirectory(parent) || ! Files.isWritable(parent)) {
			throw new FileNotFoundException("Cannot write to " + file + " because " + parent + " is not writable");
		}
		return file;
		
	}
}
