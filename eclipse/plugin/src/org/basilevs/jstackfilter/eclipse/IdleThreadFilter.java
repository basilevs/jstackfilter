package org.basilevs.jstackfilter.eclipse;

import org.eclipse.jdt.debug.core.IJavaThread;

public class IdleThreadFilter extends BackgroundViewerFilter {
	
	@Override
	protected void schedule(Object element, Runnable runnable) {
		if (!(element instanceof IJavaThread)) {
			return;
		}
		IJavaThread thread = (IJavaThread) element;
		thread.queueRunnable(runnable);
	}
	
	@Override
	protected boolean select(Object element) {
		if (!(element instanceof IJavaThread)) {
			return true;
		}
		IJavaThread thread = (IJavaThread) element;
		boolean idle = ThreadAdapter.computeIdle(thread);
		return !idle;
	}
}
