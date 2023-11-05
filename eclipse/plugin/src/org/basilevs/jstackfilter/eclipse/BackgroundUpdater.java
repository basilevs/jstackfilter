package org.basilevs.jstackfilter.eclipse;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;

public class BackgroundUpdater<T> implements Supplier<T> {
	private final AtomicReference<T> state = new AtomicReference<>();
	private final ThrottledRunnable exclusiveRunnable ;
	private final Supplier<T> compute;
	private final Runnable listener;
	
	public BackgroundUpdater(Executor executor, Supplier<T> compute, T initialState, Runnable listener) {
		this.compute = compute;
		this.listener = listener;
		state.set(initialState);
		exclusiveRunnable = new ThrottledRunnable(this::doUpdate, executor);
	}

	@Override
	public T get() {
		exclusiveRunnable.run();
		return state.get();
	}
	
	private void doUpdate() {
		T newState  = compute.get();
		T oldState = state.getAndSet(newState);
		if (!Objects.equals(newState, oldState)) {
			listener.run();
		}
	}
	
}
