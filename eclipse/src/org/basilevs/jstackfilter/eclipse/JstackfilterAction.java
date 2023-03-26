package org.basilevs.jstackfilter.eclipse;

import java.io.IOException;
import java.io.Reader;
import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.HashMap;
import java.util.Map;
import java.util.List;
import java.util.Set;
import java.util.WeakHashMap;
import java.util.HashSet;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.Filter;
import org.basilevs.jstackfilter.Frame;
import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.JstackParser;
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
import org.eclipse.jface.viewers.StructuredViewer;

public class JstackfilterAction extends AbstractThreadsViewFilterAction {
	private final LinkedHashSet<JDIDebugTarget> requests = new LinkedHashSet<>();
	private final Map<JDIDebugTarget, Set<Long>> idleThreads = new WeakHashMap<>(); 
	
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
				if (monitor.isCanceled()) {
					return Status.CANCEL_STATUS;
				}
				long pid = target.getVM().process().pid();
				Set<Long> result;
				try (Reader reader = org.basilevs.jstackfilter.ExternalJstack.read(pid)) {
					result = JstackParser.parseThreads(reader)
					.filter(Known::isKnown)
					.map(JavaThread::id)
					.collect(Collectors.toSet());
				} catch (IOException e) {
					return Status.error("Jstack failed for PID " + pid, e);
				}
				
				Set<Long> previous;
				synchronized (idleThreads) {
					previous = idleThreads.put(target, result);
				}
				if (!previous.equals(result)) {
					StructuredViewer viewer = getStructuredViewer();
					var control = viewer.getControl();
					if (control.isDisposed())
						continue;
					control.getDisplay().asyncExec(() -> {
						if (!control.isDisposed()) {
							viewer.refresh(target);
						}
					});
				}
			}
			
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
		
		synchronized (idleThreads) {
			return idleThreads.getOrDefault(jdiThread.getJavaDebugTarget(), Collections.emptySet()).contains(jdiThread.getUnderlyingThread().uniqueID());
		}
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
	
	@Override
	public void dispose() {
		refreshJob.cancel();
		super.dispose();
	}

}
