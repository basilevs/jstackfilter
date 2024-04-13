package org.basilevs.jstackfilter.eclipse;

import java.util.Collections;

import org.eclipse.core.commands.AbstractHandlerWithState;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.State;
import org.eclipse.ui.handlers.HandlerUtil;

public class ShowIdleThreadsHandler extends AbstractHandlerWithState {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		HandlerUtil.toggleCommandState(event.getCommand());
		PartManager.updatePartFromCommand(HandlerUtil.getActivePartChecked(event));
		return null;
	}
	
	public static ExecutionEvent adapt(Object evaluationContext) {
		return new ExecutionEvent(null, Collections.emptyMap(), null, evaluationContext);
	}

	@Override
	public void handleStateChange(State state, Object oldValue) {
	}


}
