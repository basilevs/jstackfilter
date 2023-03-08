package org.basilevs.jstackfilter.ui;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.KeyEventDispatcher;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.io.StringWriter;
import java.lang.ProcessBuilder.Redirect;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.function.Consumer;

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

public class Application {

	private Consumer<String> setError;

	private void handleError(Exception e) {
		e.printStackTrace();
		var text = new StringWriter();
		e.printStackTrace(new PrintWriter(text));
		setError.accept(text.toString());
	}

	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event-dispatching thread.
	 */
	private void createAndShowGUI() {
		// Create and set up the window.
		JFrame frame = new JFrame("jstackfilter");
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		KeyboardFocusManager.getCurrentKeyboardFocusManager()
		  .addKeyEventDispatcher(new KeyEventDispatcher() {
		      @Override
		      public boolean dispatchKeyEvent(KeyEvent e) {
		    	if (e.getKeyCode() == KeyEvent.VK_ESCAPE) {
		    		frame.dispose();
		    	}
		        return false;
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
			table.setModel(jpsTableModel());
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
					text.setText(Application.toString(processJstack(jstackByPid(pid))));
				} catch (IOException|IllegalArgumentException e1) {
					handleError(e1);
				}
			}
		});
		
		Dimension size = new Dimension(
		        table.getPreferredSize().width,
		        table.getRowHeight() * table.getModel().getRowCount());
		table.setPreferredScrollableViewportSize(size);
		table.setMaximumSize(size);

		// Display the window.
		frame.pack();
		frame.setVisible(true);
	}

	private static String toString(Reader input) {
		try (var scanner = new Scanner(input)) {
			scanner.useDelimiter("\\A");
			if (!scanner.hasNext()) {
				return "";
			}
			return scanner.next();
		}
	}

	private static Reader jstackByPid(int pid) throws IOException {
		return new InputStreamReader(captureOutput("jstack", "" + pid), StandardCharsets.UTF_8);
	}

	private static Reader processJstack(Reader input) {
		return ProcessInput.filter(input);
	}

	private static InputStream captureOutput(String... commandLine) throws IOException {
		var pb = new ProcessBuilder();
		pb.command(commandLine);
		pb.redirectError(Redirect.INHERIT);
		Process process = pb.start();
		return onClose(process.getInputStream(), process::destroy);
	}

	private static InputStream onClose(InputStream delegate, Runnable runnable) {
		return new FilterInputStream(delegate) {
			@Override
			public void close() throws IOException {
				super.close();
				runnable.run();
			}
		};
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

	private static TableModel jpsTableModel() throws IOException {
		var rows = new ArrayList<Object[]>();
		try (Scanner lines = new Scanner(captureOutput("jps", "-v"), StandardCharsets.UTF_8)) {
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
					rows.add(new Object[] { pid, rest });
				}
			}
		}
		TableModel model = new AbstractTableModel() {
			private static final long serialVersionUID = -5401119664597487084L;

			@Override
			public Object getValueAt(int rowIndex, int columnIndex) {
				return rows.get(rowIndex)[columnIndex];
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

	public static void main(String[] args) {
		// Schedule a job for the event-dispatching thread:
		// creating and showing this application's GUI.
		javax.swing.SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				var a = new Application();
				a.createAndShowGUI();
			}
		});
	}
}