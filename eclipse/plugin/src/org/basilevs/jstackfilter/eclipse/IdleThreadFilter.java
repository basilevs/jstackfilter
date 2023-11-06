package org.basilevs.jstackfilter.eclipse;

import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jface.viewers.Viewer;

public class IdleThreadFilter extends BackgroundViewerFilter {
	
	public IdleThreadFilter() {
		super(10000);
	}

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		if (element instanceof IJavaThread thread) {
			if (!ThreadAdapter.mightBeIdle(thread)) {
				return true;
			}
			return super.select(viewer, parentElement, element);
		}
		return true;
	}
	
	@Override
	protected Runnable createScheduler(Object element, Runnable runnable) {
		if (!(element instanceof IJavaThread)) {
			return () -> {};
		}
		IJavaThread thread = (IJavaThread) element;
		return () -> thread.queueRunnable(runnable);
	}
	
	@Override
	protected boolean select(Object element) {
		if (element instanceof IJavaThread thread) {
			boolean idle = ThreadAdapter.computeIdle(thread);
			return !idle;
		}
		return true;
	}
}
