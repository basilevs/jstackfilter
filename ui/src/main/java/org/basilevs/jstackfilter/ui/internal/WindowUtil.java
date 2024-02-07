package org.basilevs.jstackfilter.ui.internal;

import java.awt.Window;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

import javax.swing.JComponent;
import javax.swing.KeyStroke;

public class WindowUtil {
	public static void configureSize(Preferences preferences, Window window) {
		window.setSize(preferences.getInt("width", 800), preferences.getInt("height", 600));
		window.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				try {
					var size = window.getSize();
					preferences.putInt("width", size.width);
					preferences.putInt("height", size.height);
				} catch (Exception e1) {
					throw new IllegalStateException(e1);
				}
			}
		});
	}
	
	public static void handleKeystrokes(JComponent handler, String actionCommand, KeyStroke ... keys) {
		for (var key: keys) {
			handler.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(key, actionCommand);
		}
	}
	
}
