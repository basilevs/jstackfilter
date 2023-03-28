package org.basilevs.jstackfilter.eclipse;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.stream.Collectors;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.Known;
import org.basilevs.jstackfilter.eclipse.jdt.AbstractThreadsViewFilterAction;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.eclipse.jface.viewers.StructuredViewer;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

public class JstackfilterAction extends AbstractThreadsViewFilterAction {
	private static final ILog LOG = Platform.getLog(JstackfilterAction.class);
	private final Map<IJavaThread, Boolean> idleThreads = new WeakHashMap<>();

	private void updateIdle(IJavaThread thread, boolean isIdle) {
		Boolean previous;
		synchronized (idleThreads) {
			previous = idleThreads.put(thread, isIdle);
		}
		if (previous == null) {
			previous = Boolean.FALSE;
		}

		if (previous.booleanValue() == isIdle) {
			return;
		}

		StructuredViewer viewer = getStructuredViewer();
		var control = viewer.getControl();
		if (control.isDisposed())
			return;
		control.getDisplay().asyncExec(() -> {
			if (!control.isDisposed()) {
				viewer.refresh(thread.getDebugTarget(), false);
				try {
					viewer.refresh(thread.getThreadGroup(), false);
				} catch (DebugException e) {
					LOG.error("Can't get thread group", e);
				}
			}
		});
	}

	private boolean isIdle(IJavaThread thread) {
		List<StackFrame> frames;
		synchronized (thread) {
			if (thread.isSuspended()) {
				return false;
			}

			if (!thread.canSuspend()) {
				return false;
			}

			if (thread.isPerformingEvaluation())
				return false;

			if (!(thread instanceof JDIThread)) {
				return false;
			}
			JDIThread jdiThread = (JDIThread) thread;

			ThreadReference underlyingThread = jdiThread.getUnderlyingThread();
			underlyingThread.suspend();
			try {
				frames = underlyingThread.frames();
			} catch (IncompatibleThreadStateException e) {
				LOG.error("Failed to get frames", e);
				return false;
			} finally {
				underlyingThread.resume();
			}
		}

		boolean result = Known.isKnown(adapt(frames));
		return result;

	}

	private JavaThread adapt(List<StackFrame> frames) {
		return new JavaThread("irrelevant", 0, "irrelevant",
				frames.stream().<Frame>map(JstackfilterAction::adapt).collect(Collectors.toUnmodifiableList()));
	}

	@Override
	protected boolean isCandidateThread(IJavaThread thread) throws DebugException {
		return true;
	}

	@Override
	protected boolean selectThread(IJavaThread thread) throws DebugException {
		thread.queueRunnable(() -> {
			updateIdle(thread, isIdle(thread));
		});

		synchronized (idleThreads) {
			Boolean result = idleThreads.getOrDefault(thread, false);
			return !result;
		}
	}

	private static Frame adapt(StackFrame frame) {
		Location location = frame.location();
		return new Frame(location.declaringType().name() + "." + location.method().name(), "irrelevant");
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
