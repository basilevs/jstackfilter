package org.basilevs.jstackfilter.eclipse;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
import java.util.function.Function;

import org.eclipse.core.runtime.ICoreRunnable;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TreePath;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public abstract class BackgroundViewerFilter extends ViewerFilter {
	private final Map<Object, Boolean> states = new WeakHashMap<>();
	private StructuredViewer viewer;
	private final Function<Runnable, Runnable> throttledDisplayScheduler;
	private final Job fullRefreshJob ;
	private final long refreshInterval;

	public BackgroundViewerFilter(long refreshInterval) {
		this.refreshInterval = refreshInterval;
		Function<Runnable, Runnable> scheduler = r -> () -> asyncExec(r);
		scheduler = new BatchScheduler(scheduler); // group up multiple refreshes in a single redraw operation
		this.throttledDisplayScheduler = scheduler;
		
		Runnable refresh = throttledDisplayScheduler.apply(() -> viewer.refresh(false));
		
		fullRefreshJob = Job.createSystem("Full refresh for " + this,
				(ICoreRunnable) monitor -> {
					refresh.run();
				});
		fullRefreshJob.setPriority(Job.DECORATE);
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
		
		
		
		final Object lastSegment;
		if (parentElement instanceof TreePath) {
			lastSegment = ((TreePath) parentElement).getLastSegment();
		} else {
			lastSegment = parentElement;
		}
		
		createScheduler(element, () -> {
			boolean result = doSelect(element);
			boolean changed;
			synchronized(states) {
				Boolean old = states.put(element, result);
				changed = !Objects.equals(result, old);
			}
			if (changed) {
				scheduleRefresh(lastSegment);
			}
		}).run();
		Boolean state;
		synchronized (states) {
			state = states.getOrDefault(element, Boolean.TRUE);
		}
		return state;
	}

	private boolean doSelect(Object element) {
		fullRefreshJob.cancel();
		try {
			return select(element);
		} finally {
			if (refreshInterval > 0) {
				fullRefreshJob.schedule(refreshInterval);
			}
		}
	}

	private void scheduleRefresh(Object element) {
		throttledDisplayScheduler.apply(() -> refresh(element)).run();
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
			viewerCopy.getControl().setRedraw(false);
			try {
				runnable.run();
			} finally {
				viewerCopy.getControl().setRedraw(true);
			}
		});
	}
}
