package org.basilevs.jstackfilter.eclipse;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.Known;
import org.basilevs.jstackfilter.eclipse.jdt.AbstractThreadsViewFilterAction;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.internal.debug.core.model.JDIDebugTarget;
import org.eclipse.jdt.internal.debug.core.model.JDIThread;

public class JstackfilterAction extends AbstractThreadsViewFilterAction {
	private final LinkedHashSet<JDIDebugTarget> requests = new LinkedHashSet<>();
	private final Map<JDIDebugTarget, Set<Integer>> idleThreads = new WeakHashMap<>(); 
	
	private final Job refreshJob = new Job("Detect idle threads") {
		{
			setPriority(Job.LONG);
			setUser(false);
		}

		@Override
		protected IStatus run(IProgressMonitor monitor) {
			for (;;) {
				JDIDebugTarget target;
				synchronized (requests) {
					var i = requests.iterator();
					if (!i.hasNext()) {
						return Status.OK_STATUS;
					}
					target = i.next();
					i.remove();
				}
				long pid = target.getVM().process().pid();
				
			}
			
			return null;
		}
		
	};

	@Override
	protected boolean isCandidateThread(IJavaThread thread) throws DebugException {
		return true;
	}

	@Override
	protected boolean selectThread(IJavaThread thread) throws DebugException {
		if (!(thread instanceof JDIThread)) {
			return false;
		}
		
		JDIThread jdiThread = (JDIThread) thread;
		
		synchronized (requests) {
			requests.add(jdiThread.getJavaDebugTarget());
			refreshJob.schedule();
		}
		
		if (thread.isSuspended()) {
			return false;
		}
		
		
		
		List<Frame> frames = jdiThread.computeNewStackFrames().stream().filter(IJavaStackFrame.class::isInstance)
				.map(IJavaStackFrame.class::cast).map(JstackfilterAction::adapt)
				.collect(Collectors.toUnmodifiableList());
		if (frames.isEmpty())
			return true;
		JavaThread adapter = new JavaThread("irrelevant", "irrelevant", frames);
		boolean result = Known.isKnown(adapter);
		return result;
	}

	private static Frame adapt(IJavaStackFrame frame) {
		try {
			return new Frame(frame.getDeclaringTypeName()+"." + frame.getMethodName(), "irrelevant");
		} catch (DebugException e) {
			return new Frame("invalid", "irrelevant");
		}
	}

	@Override
	protected String getPreferenceKey() {
		return "show_mundane_threads";
	}

}
