package org.basilevs.jstackfilter.eclipse;

import java.io.IOException;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.JavaThread;
import org.basilevs.jstackfilter.ThreadRegistry;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

public final class Activator extends AbstractUIPlugin {
	private final ThreadRegistry idleThreads;
	private static  Activator DEFAULT = null;

	public Activator() {
		try {
			idleThreads = ThreadRegistry.idle();
		} catch (IOException e) {
			throw new IllegalStateException(e); 
		}
		DEFAULT = this;
	}
	
	public boolean isIdle(JavaThread thread) {
		return idleThreads.contains(thread);
	}
	
	public void markIdle(Stream<JavaThread> threads) {
		idleThreads.addAll(threads);
	}
	
	public void unmarkIdle(Stream<JavaThread> threads) {
		idleThreads.removeAll(threads);
	}
	
	@Override
	public void stop(BundleContext context) throws Exception {
		idleThreads.close();
		super.stop(context);
	}

	public static Activator getDefault() {
		return DEFAULT;
	}
}
