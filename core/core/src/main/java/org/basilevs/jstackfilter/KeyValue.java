package org.basilevs.jstackfilter;

import java.util.Objects;

public class KeyValue {
	public final String key;
	public final String value;
	public KeyValue(String key, String value) {
		super();
		this.key = Objects.requireNonNull(key);
		this.value = Objects.requireNonNull(value);
	}
	
}
