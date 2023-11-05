package org.basilevs.awtRunsForever;

import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.swing.JFrame;
import javax.swing.SwingUtilities;

public class Application {
	private JFrame frame;
	private final ExecutorService executor = Executors.newFixedThreadPool(1);

	public static void main(String[] args) {
		var a = new Application();
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				a.createAndShowGUI();
				a.executor.execute(() -> {
					try {
						Thread.sleep(1000);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						throw new IllegalStateException(e);
					}
//					SwingUtilities.invokeLater(() -> {
//						a.frame.dispose(); // Imitate user closing the window					
//					});
				});

			}
		});
	}

	private void close() {
		executor.shutdownNow();
		System.out.println("This method should be called after the window is closed");
	}

	protected void createAndShowGUI() {
		frame = new JFrame();
		frame.setSize(500, 500);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				frame.dispose();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				close();
			}
		});
		executor.execute(() -> {
		});
		frame.setVisible(true);

	}
}
