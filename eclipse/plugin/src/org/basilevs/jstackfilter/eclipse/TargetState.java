package org.basilevs.jstackfilter.eclipse;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.BooleanSupplier;
import java.util.function.Predicate;
import java.util.stream.Collectors;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.OperationCanceledException;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.IJobFunction;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.osgi.framework.FrameworkUtil;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

/**
 * Detects idle threads.
 * 
 * Uses internal org.eclipse.jdt.internal.debug.core.model.JDIThread.getUnderlyingThread()
 * 
 */
final class TargetState {
	private final IDebugTarget target;
	private final Set<IThread> idleThreads = new HashSet<>();
	private final Runnable listener;
	private static final ILog LOG = Platform.getLog(TargetState.class);
	private static final boolean DEBUG = Platform
			.getDebugBoolean(FrameworkUtil.getBundle(TargetState.class).getSymbolicName() + "/" + "targetState");
	private final Job job = Job.create("Searching for idle threads", (IJobFunction) (monitor -> {
		try {
			compute(monitor::isCanceled);
		} catch (DebugException e) {
			return Status.error("Failed to handle a debug target", e);
		}
		return Status.OK_STATUS;
	}));
	private final Predicate<JavaThread> isThreadIdle;

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

	public TargetState(IDebugTarget target, Predicate<JavaThread> isThreadIdle, Runnable listener) {
		this.isThreadIdle = Objects.requireNonNull(isThreadIdle);
		this.target = Objects.requireNonNull(target);
		this.listener = Objects.requireNonNull(listener);
		job.setUser(false);
		job.setPriority(Job.LONG);
	}

	public boolean isIdle(IThread thread) {
		if (!getJdi(thread).isPresent()) {
			return false;
		}
		job.cancel();
		job.schedule();
		synchronized (idleThreads) {
			return idleThreads.contains(thread);
		}
	}

	@SuppressWarnings("restriction")
	private static Optional<ThreadReference> getJdi(IThread thread) {
		if (!(thread instanceof org.eclipse.jdt.internal.debug.core.model.JDIThread)) {
			return Optional.empty();
		}
		var jdiThread = (org.eclipse.jdt.internal.debug.core.model.JDIThread) thread;
		return Optional.ofNullable(jdiThread.getUnderlyingThread());
	}

	private boolean computeIdle(IThread thread) {
		List<StackFrame> frames;
		synchronized (thread) {
			if (thread.isSuspended()) {
				return false;
			}

			if (!thread.canSuspend()) {
				return false;
			}

			if (((IJavaThread) thread).isPerformingEvaluation())
				return false;

			frames = getJdi(thread).<List<StackFrame>>map((ThreadReference underlyingThread) -> {
				underlyingThread.suspend();
				try {
					return underlyingThread.frames();
				} catch (IncompatibleThreadStateException e) {
					LOG.error("Failed to get frames", e);
					return Collections.emptyList();
				} catch (ObjectCollectedException e) {
					return Collections.emptyList();
				} finally {
					underlyingThread.resume();
				}
			}).orElse(Collections.emptyList());
		}

		if (frames.isEmpty()) {
			return false;
		}

		JavaThread threadCandidate = adapt(frames);
		boolean idle = isThreadIdle.test(threadCandidate);
		if (DEBUG) {
			try {
				LOG.info(thread.getName() + (idle ? " is idle" : " is not idle"));
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