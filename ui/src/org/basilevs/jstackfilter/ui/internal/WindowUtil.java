package org.basilevs.jstackfilter.ui.internal;

import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.Window;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.prefs.Preferences;

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

	public static void closeOnEsc(Window frame) {
		KeyboardFocusManager.getCurrentKeyboardFocusManager().addKeyEventDispatcher(new KeyEventDispatcher() {
			@Override
			public boolean dispatchKeyEvent(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
					frame.dispose();
				}
				return true;
			}
		});
	}

}
