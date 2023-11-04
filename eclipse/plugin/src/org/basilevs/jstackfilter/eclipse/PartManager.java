package org.basilevs.jstackfilter.eclipse;

import java.util.Arrays;
import java.util.Optional;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.debug.ui.IDebugView;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.RegistryToggleState;

public final class PartManager {

	private PartManager() {

	}

	public static void updateCommandFromPart(IWorkbenchPart part) throws ExecutionException {
		StructuredViewer viewer = getViewer(part);
		getCommandState(part).setValue(findFilter(viewer).isEmpty());
	}
	
	public static void updatePartFromCommand(IWorkbenchPart part) throws ExecutionException {
		StructuredViewer viewer = getViewer(part);
		boolean checked = (boolean) getCommandState(part).getValue();
		if (!checked) {
			if (findFilter(viewer).isEmpty()) {
				viewer.addFilter(new IdleThreadFilter());
			}
		} else {
			findFilter(viewer).ifPresent(viewer::removeFilter);
		}
	}

	private static StructuredViewer getViewer(IWorkbenchPart part) throws ExecutionException {
		IDebugView view = part.getAdapter(IDebugView.class);
		if (view == null) {
			throw new ExecutionException("Not a Debug view: " + part.getSite().getId() );
		}
		StructuredViewer viewer = (StructuredViewer) view.getViewer();
		return viewer;
	}
	
	private static State getCommandState(IWorkbenchPart part) {
		Command command = part.getSite().getService(ICommandService.class)
				.getCommand("org.basilevs.jstackfilter.eclipse.ui.showIdle");
		return command.getState(RegistryToggleState.STATE_ID);
	}

	private static Optional<IdleThreadFilter> findFilter(StructuredViewer viewer) {
		return Arrays.stream(viewer.getFilters()).filter(IdleThreadFilter.class::isInstance)
				.map(IdleThreadFilter.class::cast).findFirst();
	}

}
