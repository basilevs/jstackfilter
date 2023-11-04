package org.basilevs.jstackfilter.eclipse;

import java.util.Collections;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.handlers.HandlerUtil;

public class ShowIdleThreadsHandler extends AbstractHandler {

	@Override
	public void setEnabled(Object evaluationContext) {
		super.setEnabled(evaluationContext);
		var event = new ExecutionEvent(null, Collections.emptyMap(), null, evaluationContext);
		try {
			PartManager.updateCommandFromPart(HandlerUtil.getActivePartChecked(event));
		} catch (ExecutionException e) {
			throw new IllegalStateException(e);
		}
	}

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		HandlerUtil.toggleCommandState(event.getCommand());
		PartManager.updatePartFromCommand(HandlerUtil.getActivePartChecked(event));
		return null;
	}
	
	public static ExecutionEvent adapt(Object evaluationContext) {
		return new ExecutionEvent(null, Collections.emptyMap(), null, evaluationContext);
	}


}
