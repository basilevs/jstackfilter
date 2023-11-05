package org.basilevs.jstackfilter.eclipse;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Function;
import java.util.function.Supplier;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public abstract class BackgroundViewerFilter extends ViewerFilter {
	private final Map<Object, Supplier<Boolean>> states = new WeakHashMap<>();
	private final Map<Object, Runnable> refreshers = new WeakHashMap<>();
	private StructuredViewer viewer;
	private final Function<Runnable, Runnable> throttledDisplayScheduler;
	
	public BackgroundViewerFilter() {
		Function<Runnable, Runnable> scheduler = r -> () -> asyncExec(r);
		scheduler = new ThrottledScheduler(scheduler);
		this.throttledDisplayScheduler = scheduler;
		
	}

	/**
	 * Override this method to customize computation scheduling
	 * 
	 * @param element - context of execution
	 * @return a runnable that will schedule given operation when called
	 */
	protected Runnable createScheduler(Object element, Runnable runnable) {
		var job = new Job("Filtering...") {
			@Override
			public IStatus run(IProgressMonitor monitor) {
				runnable.run();
				return Status.OK_STATUS;
			}
		};
		job.setUser(false);
		job.setSystem(true);
		job.setPriority(Job.LONG);
		return job::schedule;
	}

	protected abstract boolean select(Object element);

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		Objects.requireNonNull(viewer);
		if (this.viewer != null && viewer != this.viewer) {
			throw new IllegalArgumentException("Multiple viewers are not supported");
		}
		this.viewer = (StructuredViewer) viewer;
		Supplier<Boolean> state;
		synchronized (states) {
			state = states.computeIfAbsent(element, (t) -> createState(parentElement, t));
		}
		Boolean result = state.get();
		return result;
	}

	private Supplier<Boolean> createState(Object parentElement, Object element) {
		final Object lastSegment;
		if (parentElement instanceof TreePath) {
			lastSegment = ((TreePath) parentElement).getLastSegment();
		} else {
			lastSegment = parentElement;
		}
		Function<Runnable, Runnable> scheduler = runnable -> createScheduler(element, runnable);
		scheduler = new ThrottledScheduler(scheduler);
		return new BackgroundUpdater<>(scheduler, () -> select(element),
				Boolean.TRUE, () -> scheduleRefresh(lastSegment));
	}

	private void scheduleRefresh(Object element) {
		Runnable schedule;
		synchronized (refreshers) {
			schedule = refreshers.computeIfAbsent(element, e -> throttledDisplayScheduler.apply(() -> refresh(e)));
		}
		schedule.run();
	}
	
	private void refresh(Object element) {
		viewer.refresh(element, false);
	}

	private void asyncExec(Runnable runnable) {
		StructuredViewer viewerCopy = viewer;
		if (viewerCopy == null) {
			return;
		}
		var control = viewerCopy.getControl();
		if (control.isDisposed()) {
			return;
		}
		var display = control.getDisplay();
		display.asyncExec(() -> {
			if (control.isDisposed()) {
				return;
			}
			runnable.run();
		});
	}
}
