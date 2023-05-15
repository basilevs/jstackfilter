package org.basilevs.jstackfilter.eclipse;

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.debug.core.model.IThread;
import org.eclipse.jdt.debug.core.IJavaThread;

import com.sun.jdi.IncompatibleThreadStateException;
import com.sun.jdi.Location;
import com.sun.jdi.ObjectCollectedException;
import com.sun.jdi.StackFrame;
import com.sun.jdi.ThreadReference;

public final class ThreadAdapter {
	private static final ILog LOG = Platform.getLog(ThreadAdapter.class);

	private ThreadAdapter() {
		throw new UnsupportedOperationException();
	}
	
	public static final class Snapshot {
		private final List<StackFrame> frames;

		private Snapshot(List<StackFrame> frames) {
			super();
			this.frames = Objects.requireNonNull(frames);
		}
		
		public Optional<JavaThread> computeJavaThread() {
			if (frames.isEmpty()) {
				return Optional.empty();
			}
			return Optional.of(adapt(frames));
		}
		
	}

	public static Snapshot snapshot(IJavaThread thread) {
		List<StackFrame> frames;
		synchronized (thread) {			
			frames = getJdi(thread).<List<StackFrame>>map((ThreadReference underlyingThread) -> {
				boolean wasSuspended = underlyingThread.isSuspended();
				if (!wasSuspended) {
					underlyingThread.suspend();
				}
				try {
					return underlyingThread.frames();
				} catch (IncompatibleThreadStateException e) {
					LOG.error("Failed to get frames", e);
					return Collections.emptyList();
				} catch (ObjectCollectedException e) {
					return Collections.emptyList();
				} finally {
					if (!wasSuspended) {
						underlyingThread.resume();
					}
				}
			}).orElse(Collections.emptyList());
		}
		return new Snapshot(frames);
	}

	private static JavaThread adapt(List<StackFrame> frames) {
		return new JavaThread("irrelevant", 0, "irrelevant",
				frames.stream().<Frame>map(ThreadAdapter::adapt).collect(Collectors.toUnmodifiableList()));
	}

	private static Frame adapt(StackFrame frame) {
		Location location = frame.location();
		return new Frame(location.declaringType().name() + "." + location.method().name(), "irrelevant");
	}

	@SuppressWarnings("restriction")
	private static Optional<ThreadReference> getJdi(IThread thread) {
		if (!(thread instanceof org.eclipse.jdt.internal.debug.core.model.JDIThread)) {
			return Optional.empty();
		}
		var jdiThread = (org.eclipse.jdt.internal.debug.core.model.JDIThread) thread;
		return Optional.ofNullable(jdiThread.getUnderlyingThread());
	}
}
