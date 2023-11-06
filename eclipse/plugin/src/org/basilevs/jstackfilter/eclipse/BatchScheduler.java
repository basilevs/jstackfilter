package org.basilevs.jstackfilter.eclipse;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Function;

public class BatchScheduler implements Function<Runnable, Runnable> {
	private final Runnable schedule;
	private Queue<Runnable> queue = new ConcurrentLinkedQueue<>();

	/**
	 * 
	 * @param scheduler - returns a schedule handler for a given operation, the
	 *                  handler schedules operation to run when called
	 */
	public BatchScheduler(Function<Runnable, Runnable> scheduler) {
		this.schedule = scheduler.apply(this::process);
	}
	
	private void process() {
		for (;;) {
			Runnable runnnable = queue.poll();
			if (runnnable == null) {
				return;
			}
			runnnable.run();
		}
	}

	@Override
	public Runnable apply(Runnable runnable) {
		return () -> {
			queue.add(runnable);
			schedule.run();
		};
	}

}
