package org.basilevs.jstackfilter.eclipse;

import java.util.Arrays;
import java.util.function.Predicate;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.handlers.HandlerUtil;

public class ShowIdleThreadsHandler extends AbstractHandler {
	
	private final IdleThreadFilter filter = new IdleThreadFilter();
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean checked = HandlerUtil.toggleCommandState(event.getCommand());
		StructuredViewer viewer = getStructuredViewer(event);
		if (checked) {
			if (!hasFilter(viewer)) { 
				viewer.addFilter(filter);
			}
		} else {
			viewer.removeFilter(filter);
		}
		return null;
	}

	private boolean hasFilter(StructuredViewer viewer) {
		return Arrays.stream(viewer.getFilters()).anyMatch(Predicate.isEqual(filter));
	}
	
	protected StructuredViewer getStructuredViewer(ExecutionEvent event) {
		IDebugView view = HandlerUtil.getActivePart(event).getAdapter(IDebugView.class);
		Viewer viewer = view.getViewer();
		if (viewer instanceof StructuredViewer) {
			return (StructuredViewer)viewer;
		}
		throw new IllegalArgumentException("Is not called from debug view");
	}

}
