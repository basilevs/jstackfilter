package org.basilevs.jstackfilter.eclipse;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.stream.Collectors;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.Known;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;
import org.osgi.framework.FrameworkUtil;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

final class TargetState {
	private final IDebugTarget target;
	private final Set<IThread> idleThreads = new HashSet<>();
	private final Runnable listener;
	private static final ILog LOG = Platform.getLog(TargetState.class); 
	private static final boolean DEBUG = Platform.getDebugBoolean(FrameworkUtil.getBundle(TargetState.class).getSymbolicName()+"/"+"targetState");
	private final Job job = Job.create("Searching for idle threads", (IJobFunction) (monitor -> {
		try {
			compute(monitor::isCanceled);
		} catch (DebugException e) {
			return Status.error("Failed to handle a debug target", e);
		}
		return Status.OK_STATUS;
	}));

	private void compute(BooleanSupplier isCancelled) throws DebugException {
		var result = Arrays.stream(target.getThreads()).filter(this::computeIdle).peek(item -> {
			if (isCancelled.getAsBoolean())
				throw new OperationCanceledException();
		}).collect(Collectors.toUnmodifiableSet());
		boolean equal;
		synchronized (idleThreads) {
			equal = idleThreads.equals(result);
			idleThreads.clear();
			idleThreads.addAll(result);
			
		}
		if (!equal) {
			listener.run();
		}
	}

	public TargetState(IDebugTarget target, Runnable listener) {
		this.target = Objects.requireNonNull(target);
		this.listener = Objects.requireNonNull(listener);
		job.setUser(false);
		job.setPriority(Job.LONG);
	}

	public boolean isIdle(IThread thread) {
		if (!(thread instanceof JDIThread)) {
			return false;
		}
		job.cancel();
		job.schedule();
		synchronized (idleThreads) {
			return idleThreads.contains(thread);
		}
	}

	@SuppressWarnings("restriction")
	private boolean computeIdle(IThread thread) {
		List<StackFrame> frames;
		if (!(thread instanceof JDIThread)) {
			return false;
		}

		JDIThread jdiThread = (JDIThread) thread;
		
		synchronized (thread) {
			if (thread.isSuspended()) {
				return false;
			}

			if (!thread.canSuspend()) {
				return false;
			}

			if (jdiThread.isPerformingEvaluation())
				return false;

			ThreadReference underlyingThread = jdiThread.getUnderlyingThread();
			underlyingThread.suspend();
			try {
				frames = underlyingThread.frames();
			} catch (IncompatibleThreadStateException e) {
				LOG.error("Failed to get frames", e);
				return false;
			} catch (ObjectCollectedException e) {
				return false;
			} finally {
				underlyingThread.resume();
			}
		}

		boolean idle = Known.isKnown(adapt(frames));
		if (DEBUG) {
			try {
				LOG.info(thread.getName() + (idle ?  " is idle" : " is not idle"));
			} catch (DebugException e) {
				LOG.error("Failed to handle error", e);
			}
		}
		return idle;
	}

	JavaThread adapt(List<StackFrame> frames) {
		return new JavaThread("irrelevant", 0, "irrelevant",
				frames.stream().<Frame>map(TargetState::adapt).collect(Collectors.toUnmodifiableList()));
	}

	private static Frame adapt(StackFrame frame) {
		Location location = frame.location();
		return new Frame(location.declaringType().name() + "." + location.method().name(), "irrelevant");
	}

}