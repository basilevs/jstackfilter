package org.basilevs.jstackfilter;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Spliterator;
import java.util.function.Consumer;

public class PreSplitSpliterator<T> implements Spliterator<T> {
	private Spliterator<T> delegate;
	private final List<Spliterator<T>> more; 
	
	public PreSplitSpliterator(Collection<? extends Spliterator<T>> spliterators) {
		more = new ArrayList<>(spliterators);
		delegate = trySplit();
	}

	@Override
	public final boolean tryAdvance(Consumer<? super T> action) {
		for (;;) {
			if (delegate == null) {
				return false;
			}
			if (delegate.tryAdvance(action)) {
				return true;
			}
			if (more.isEmpty()) {
				return false;
			}
			delegate = trySplit();
		}
	}

	@Override
	public final Spliterator<T> trySplit() {
		if (more.isEmpty()) {
			return null;
		}
		return more.remove(more.size() - 1);
	}

	@Override
	public long estimateSize() {
		return delegate.estimateSize() + more.stream().mapToLong(Spliterator::estimateSize).reduce(0, PreSplitSpliterator::add);
	}

	@Override
	public final int characteristics() {
		return more.stream().mapToInt(Spliterator::characteristics).reduce(delegate.characteristics(), (c1, c2) -> c1 & c2);
	}
	
	private static long add(long a, long b) {
		try {
			return Math.addExact(a, b);
		} catch (ArithmeticException e) {
			return Long.MAX_VALUE;
		}
	}
}
