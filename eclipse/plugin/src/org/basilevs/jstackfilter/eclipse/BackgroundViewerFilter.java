package org.basilevs.jstackfilter.eclipse;

import java.util.Map;
import java.util.Objects;
import java.util.WeakHashMap;
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
	private StructuredViewer viewer;

	/**
	 * Override this method to customize computation scheduling
	 * 
	 * @param element - context of execution
	 */
	protected void schedule(Object element, Runnable runnable) {
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
		job.schedule();
	}

	protected abstract boolean select(Object element);

	@Override
	public final boolean select(Viewer viewer, Object parentElement, Object element) {
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
		return new BackgroundUpdater<>(runnable -> schedule(element, runnable), () -> select(element), Boolean.TRUE,
				() -> refresh(lastSegment));
	}

	private void refresh(Object element) {
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
			viewer.refresh(element, false);
		});
	}
}
