package org.basilevs.jstackfilter.eclipse;

import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;

/**
 * Skip repeated requests, execute only last request. Only last request is
 * guaranteed to be executed. Repeated requests are ignored.
 */
public final class ThrottledScheduler implements Function<Runnable, Runnable> {
	private enum State {
		IDLE, SCHEDULED, EXECUTING
	}

	private Function<Runnable, Runnable> scheduler;

	/**
	 * 
	 * @param scheduler - returns a schedule handler for a given operation, the
	 *                  handler schedules operation to run when called
	 */
	public ThrottledScheduler(Function<Runnable, Runnable> scheduler) {
		this.scheduler = scheduler;
	}

	@Override
	public Runnable apply(Runnable delegate) {
		final AtomicReference<State> state = new AtomicReference<>(State.IDLE);
		Runnable wrapper = () -> {
			do {
				State previous = state.getAndSet(State.EXECUTING);
				if (State.SCHEDULED.equals(previous)) {
					delegate.run();
				}
			} while (!state.compareAndSet(State.EXECUTING, State.IDLE));
		};
		Runnable schedule = scheduler.apply(wrapper);
		return () -> {
			State previous = state.getAndSet(State.SCHEDULED);
			if (State.IDLE.equals(previous)) {
				schedule.run();
			}
		};
	}

}
