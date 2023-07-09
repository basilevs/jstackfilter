package org.basilevs.jstackfilter.eclipse;

import java.util.Map;
import java.util.WeakHashMap;

import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerFilter;

public class IdleThreadFilter extends ViewerFilter {
	static final ILog LOG = Platform.getLog(IdleThreadFilter.class);
	private final Map<IDebugTarget, TargetState> targetStates = new WeakHashMap<>();

	@Override
	public boolean select(Viewer viewer, Object parentElement, Object element) {
		StructuredViewer structuredViewer = (StructuredViewer) viewer;
		if (!(element instanceof IJavaThread)) {
			return true;
		}
		IJavaThread thread = (IJavaThread) element;
		var target = thread.getDebugTarget();
		TargetState state;
		synchronized (targetStates) {
			state = targetStates.computeIfAbsent(target, (t) -> new TargetState(t, Activator.getDefault()::isIdle, () -> this.refresh(structuredViewer, t)));
		}
		return !state.isIdle(thread);
	}
	
	private void refresh(StructuredViewer viewer, IDebugTarget target) {
		var control = viewer.getControl();
		if (control.isDisposed()) {
			return;
		}
		var display = control.getDisplay();
		display.asyncExec(() -> {
			if (control.isDisposed()) {
				return;
			}
			viewer.refresh(target, false);
		});
	}
}
