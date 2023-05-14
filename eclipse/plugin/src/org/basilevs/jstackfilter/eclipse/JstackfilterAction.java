package org.basilevs.jstackfilter.eclipse;

import java.io.IOException;
import java.util.Map;
import java.util.WeakHashMap;

import org.basilevs.jstackfilter.ThreadRegistry;
import org.basilevs.jstackfilter.eclipse.jdt.AbstractThreadsViewFilterAction;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.debug.core.IJavaThread;

public class JstackfilterAction extends AbstractThreadsViewFilterAction {
	static final ILog LOG = Platform.getLog(JstackfilterAction.class);
	private final Map<IDebugTarget, TargetState> targetStates = new WeakHashMap<>();
	private final ThreadRegistry idleThreads; 
	{
		try {
			idleThreads = ThreadRegistry.idle();
		} catch (IOException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	protected boolean isCandidateThread(IJavaThread thread) throws DebugException {
		return thread.canSuspend();
	}

	@Override
	protected boolean selectThread(IJavaThread thread) throws DebugException {
		var target = thread.getDebugTarget();
		TargetState state;
		synchronized (targetStates) {
			state = targetStates.computeIfAbsent(target, (t) -> new TargetState(t, idleThreads::isKnown, () -> this.refresh(t)));
		}
		return !state.isIdle(thread);
	}
	
	private void refresh(IDebugTarget target) {
		var viewer = getStructuredViewer();
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

	@Override
	protected String getPreferenceKey() {
		return "show_mundane_threads";
	}

	@Override
	public void dispose() {
		super.dispose();
	}

}
