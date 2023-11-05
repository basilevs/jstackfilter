package org.basilevs.jstackfilter.eclipse;

import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.function.Supplier;

public class BackgroundUpdater<T> implements Supplier<T> {
	private final AtomicReference<T> state = new AtomicReference<>();
	private final Runnable schedule ;
	private final Supplier<T> compute;
	private final Runnable listener;
	
	public BackgroundUpdater(Function<Runnable, Runnable> scheduler, Supplier<T> compute, T initialState, Runnable listener) {
		this.compute = compute;
		this.listener = listener;
		state.set(initialState);
		schedule = scheduler.apply(this::doUpdate);
	}

	@Override
	public T get() {
		schedule.run();
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
