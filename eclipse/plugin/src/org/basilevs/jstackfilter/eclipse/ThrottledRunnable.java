package org.basilevs.jstackfilter.eclipse;

import java.util.Objects;
import java.util.concurrent.Executor;
import java.util.concurrent.atomic.AtomicReference;

/** 
 * Skip repeated requests, execute only last request.
 * Only last request is guaranteed to be executed.
 * Repeated requests are ignored.
 */
public final class ThrottledRunnable implements Runnable {
	private enum State {
		IDLE, SCHEDULED, EXECUTING
	}

	private final Executor executor;
	private final AtomicReference<State> state = new AtomicReference<>(State.IDLE);
	private final Runnable wrapper;

	public ThrottledRunnable(Runnable delegate, Executor executor) {
		this.executor = Objects.requireNonNull(executor);
		wrapper = () -> {
			do {
				State previous = state.getAndSet(State.EXECUTING);
				if (State.SCHEDULED.equals(previous)) {
					delegate.run();
				}
			} while (!state.compareAndSet(State.EXECUTING, State.IDLE));
		};
	}

	@Override
	public void run() {
		State previous = state.getAndSet(State.SCHEDULED);
		if (State.IDLE.equals(previous)) {
			executor.execute(wrapper);
		}
	}

}
