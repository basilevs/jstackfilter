package org.basilevs.jstackfilter;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Objects;
import java.util.function.BiPredicate;

public final class JavaThread {

	private final List<Frame> frames;
	private final String name;
	private final String state;
	private final String representation;
	private final long id;

	public JavaThread(String name, long id, String state, Collection<Frame> frames) {
		this(name, id, state, frames, buildRepresentation(name, id, state, frames));

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
		if (that == null) {
			return false;
		}
		return iteratorsEqual(frames().iterator(), that.frames().iterator(), (t1, t2) -> t1.equalByMethodName(t2));
	}
	
	public JavaThread withRepresentation(String representation) {
		return new JavaThread(this.name(), this.id(), this.state(), frames, representation);
	}

	private static String buildRepresentation(String name, long id, String state, Collection<Frame> frames) {
		StringBuilder result = new StringBuilder();
		result.append(String.format("\"%s\" #%d prio=1 [0x0000000000000000]\n   java.lang.Thread.State: %s\n", name, id, state));
		for (Frame frame : frames) {
			result.append('\t').append(frame).append('\n');
		}
		return result.toString();
	}

	public long id() {
		return id;
	}
	
    private static <T> boolean iteratorsEqual(
            Iterator<T> it1, 
            Iterator<T> it2, 
            BiPredicate<T, T> comparator) {
        
        while (it1.hasNext() && it2.hasNext()) {
            T o1 = it1.next();
            T o2 = it2.next();
            if (!comparator.test(o1, o2)) {
                return false;
            }
        }
        return !it1.hasNext() && !it2.hasNext();
    }
}
