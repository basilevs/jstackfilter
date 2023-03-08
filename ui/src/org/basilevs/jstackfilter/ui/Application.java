package org.basilevs.jstackfilter.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.BoxLayout;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;

import org.basilevs.jstackfilter.ProcessInput;
import org.basilevs.jstackfilter.ui.internal.SystemUtil;
import org.basilevs.jstackfilter.ui.internal.WindowUtil;

public class Application {
	private final Preferences prefs = Preferences.userNodeForPackage(Application.class);
	private Consumer<String> setError;
	private final ExecutorService executor = Executors.newFixedThreadPool(1);

	private Application() throws BackingStoreException {
	}
	
	private void close() {
		executor.shutdownNow();
	}
	
	private void handleError(Exception e) {
		if (e instanceof SystemUtil.ErrorOutput) {
			setError.accept(e.getMessage());	
		} else {
			e.printStackTrace();
			var text = new StringWriter();
			e.printStackTrace(new PrintWriter(text));
			setError.accept(text.toString());
		}
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event-dispatching thread.
	 */
	private void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("jstackfilter");
		WindowUtil.configureSize(prefs, frame);
		WindowUtil.closeOnEsc(frame);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				close();
			}
		});

		JTable table = new JTable();
		frame.getContentPane().setLayout(new BoxLayout(frame.getContentPane(), BoxLayout.Y_AXIS));
		table.setAutoResizeMode(JTable.AUTO_RESIZE_LAST_COLUMN);
		var tablePane = new JScrollPane(table);
		frame.getContentPane().add(tablePane);
		tablePane.setMinimumSize(new Dimension(800, 200));
		JTextArea text = new JTextArea();
		JScrollPane textPane = new JScrollPane(text);
		frame.getContentPane().add(textPane);

		Dimension minTextSize = new Dimension(800, 300);
		textPane.setPreferredSize(minTextSize);
		setError = message -> {
			text.setForeground(Color.RED);
			text.setText(message);
		};

		var defaultForeground = text.getForeground();

		try {
			table.setModel(toTableModel(getJavaProcesses()));
			table.getColumnModel().getColumn(0).setHeaderValue("PID");
			table.getColumnModel().getColumn(1).setHeaderValue("Command");
			packColumns(table);
		} catch (IOException e2) {
			handleError(e2);
		}
		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				Integer pid = (Integer) table.getModel().getValueAt(table.getSelectedRow(), 0);
				try {
					text.setForeground(defaultForeground);
					text.setText(SystemUtil.toString(jstackByPid(pid)));
				} catch (IllegalStateException e2 ) {
					var cause = e2.getCause();
					if (cause instanceof SystemUtil.ErrorOutput) {
						handleError((Exception) cause);
					} else {
						handleError(e2);
					}
				} catch (Exception e1) {
					handleError(e1);
				}
			}
		});

		Dimension size = new Dimension(table.getPreferredSize().width,
				table.getRowHeight() * table.getModel().getRowCount());
		table.setPreferredScrollableViewportSize(size);
		table.setMaximumSize(size);

		
		
		frame.setVisible(true);
	}

	private Reader jstackByPid(int pid) throws IOException {
		return ProcessInput.filter(new InputStreamReader(SystemUtil.captureOutput(executor, "jstack", "" + pid), StandardCharsets.UTF_8));
	}

	private static void packColumns(JTable table) {
		for (int column = 0; column < table.getColumnCount() - 1; column++) {
			TableColumn tableColumn = table.getColumnModel().getColumn(column);
			int preferredWidth = tableColumn.getMinWidth();
			int maxWidth = tableColumn.getMaxWidth();

			for (int row = 0; row < table.getRowCount(); row++) {
				TableCellRenderer cellRenderer = table.getCellRenderer(row, column);
				Component c = table.prepareRenderer(cellRenderer, row, column);
				int width = c.getPreferredSize().width + table.getIntercellSpacing().width;
				preferredWidth = Math.max(preferredWidth, width);

				// We've exceeded the maximum width, no need to check other rows

				if (preferredWidth >= maxWidth) {
					preferredWidth = maxWidth;
					break;
				}
			}

			tableColumn.setMaxWidth(preferredWidth);
		}
	}

	private static TableModel toTableModel(List<JavaProcess> rows) throws IOException {
		TableModel model = new AbstractTableModel() {
			private static final long serialVersionUID = -5401119664597487084L;

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				JavaProcess process = rows.get(rowIndex);
				switch (columnIndex) {
				case 0:
					return process.pid();
				case 1:
					return process.command();
				default:
					throw new IllegalArgumentException("No column " + columnIndex);
				}
			}

			@Override
			public int getRowCount() {
				return rows.size();
			}

			@Override
			public int getColumnCount() {
				return 2;
			}
		};
		return model;
	}

	private static record JavaProcess(int pid, String command) {
	}

	private ArrayList<JavaProcess> getJavaProcesses() throws IOException {
		ArrayList<JavaProcess> rows = new ArrayList<>();
		try (Scanner lines = new Scanner(SystemUtil.captureOutput(executor, "jps", "-v"), StandardCharsets.UTF_8)) {
			try {
				lines.useDelimiter("\n");
				while (lines.hasNext()) {
					Scanner fields = new Scanner(lines.next());
					fields.useDelimiter("\s+");
					if (fields.hasNext()) {
						int pid = Integer.valueOf(fields.next());
						fields.skip("\s+");
						fields.useDelimiter("\\A");
						var rest = fields.next();
						if (rest.startsWith("Jps")) {
							continue;
						}
						rows.add(new JavaProcess(pid, rest));
					}
				}
				lines.close();
			} finally {
				var error = lines.ioException();
				if (error != null) {
					throw error;
				}
			}
		}
		return rows;
	}

	public static void main(String[] args) throws BackingStoreException {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		var a = new Application();
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				a.createAndShowGUI();
			}
		});
	}
}