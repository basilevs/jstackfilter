package org.basilevs.jstackfilter.eclipse;
import java.util.Arrays;
import java.util.Collections;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.JavaThread;
import org.eclipse.core.commands.AbstractHandlerWithState;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.State;
import org.eclipse.core.runtime.Adapters;
import org.eclipse.core.runtime.ILog;
import org.eclipse.core.runtime.Platform;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jface.commands.ToggleState;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.IWorkbenchSite;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.commands.IElementUpdater;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.handlers.RegistryToggleState;
import org.eclipse.ui.menus.UIElement;

public class MarkIdleHandler extends AbstractHandlerWithState implements IHandler, IElementUpdater {
	private static final ILog LOG = Platform.getLog(MarkIdleHandler.class);
	
	
	@Override
	public void setEnabled(Object evaluationContext) {
		super.setEnabled(evaluationContext);
		boolean isRegisteredIdle;
		try {
			Stream<JavaThread> threads = getThreads(evaluationContext);
			isRegisteredIdle = threads.parallel()
					.anyMatch(Activator.getDefault()::isIdle);
		} catch (ExecutionException e) {
			LOG.error("Failed to process selected threads", e);
			isRegisteredIdle = false;
		}
		
		IWorkbenchSite site;
		try {
			site = HandlerUtil.getActiveSiteChecked(adapt(evaluationContext));
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		}
		State state = site.getService(ICommandService.class).getCommand("org.basilevs.jstackfilter.eclipse.ui.markidle").getState(RegistryToggleState.STATE_ID);
		state.setValue(isRegisteredIdle);
	}

	private Stream<JavaThread> getThreads(Object evaluationContext) throws ExecutionException {
		IStructuredSelection selection = (IStructuredSelection) HandlerUtil
				.getCurrentSelectionChecked(adapt(evaluationContext));
		Stream<JavaThread> threads = Arrays.stream(selection.toArray())
				.map(t -> Adapters.adapt(t, IJavaThread.class))
				.map(ThreadAdapter::snapshot)
				.map(ThreadAdapter.Snapshot::computeJavaThread)
				.flatMap(Optional::stream);
		return threads;
	}

	@Override
	public void handleStateChange(State state, Object oldValue) {
		return;
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean wasChecked = HandlerUtil.toggleCommandState(event.getCommand());
		boolean isRegisteredIdle = !wasChecked;
		if (isRegisteredIdle) {
			Activator.getDefault().markIdle(getThreads(event.getApplicationContext()));
		} else {
			Activator.getDefault().unmarkIdle(getThreads(event.getApplicationContext()));
		}
		return null;
	}
	
	private static ExecutionEvent adapt(Object evaluationContext) {
		return new ExecutionEvent(null, Collections.emptyMap(), null, evaluationContext);
	}

	@Override
	public void updateElement(UIElement element, Map parameters) {
		//element.setChecked(isRegisteredIdle);
	}
	
	
}
