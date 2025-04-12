package org.basilevs.jstackfilter.ui;

import static javax.swing.SwingUtilities.invokeLater;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.text.DefaultEditorKit;

import org.basilevs.jstackfilter.ui.internal.ParagraphCaret;
import org.basilevs.jstackfilter.ui.internal.WindowUtil;

public class Application {
	private final Preferences prefs = Preferences.userNodeForPackage(Application.class);
	private Consumer<String> setError;
	private Consumer<String> setOutput;
	private final Model model = new Model(this::handleError, this::handleOutput);
	private JFileChooser fileChooser;

	private Application() throws BackingStoreException {
	}

	private void handleError(String message) {
		setError.accept(message);
	}

	private void handleOutput(String output) {
		setOutput.accept(output);
	}
	
	private void handleError(Exception e) {
		e.printStackTrace();
		var text = new StringWriter();
		e.printStackTrace(new PrintWriter(text));
		handleError(text.toString());
	}


	/**
	 * Create the GUI and show it. For thread safety, this method should be invoked
	 * from the event-dispatching thread.
	 */
	private void createAndShowGUI() {
		// Create and set up the window.
		final JFrame frame = new JFrame("jstackfilter");
		WindowUtil.configureSize(prefs, frame);
		frame.addWindowListener(new WindowAdapter() {
			@Override
			public void windowClosing(WindowEvent e) {
				// On Mac OS closing window does not "terminate" the application and
				// windowClosed is not called
				// As we have no way to work without windows, we close the main window
				// completely, forcing termination
				frame.dispose();
			}

			@Override
			public void windowClosed(WindowEvent e) {
				super.windowClosed(e);
				try {
					model.close();
				} catch (IOException e1) {
					throw new RuntimeException(e1);
				}
			}
		});

		final Container content = Box.createVerticalBox();
		frame.getContentPane().add(content);
		
		var controls = Box.createHorizontalBox();
		controls.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(controls);
		var filter = new JCheckBox("filter");
		controls.add(filter);
		filter.setSelected(true);
		
		var refreshButton = new JButton();
		controls.add(refreshButton);
		
		var pasteButton = new JButton();
		controls.add(pasteButton);
		
		var loadButton = new JButton();
		controls.add(loadButton);
		
		var markIdleButton = new JButton();
		controls.add(markIdleButton);

		var split = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		split.setAlignmentX(Component.LEFT_ALIGNMENT);
		content.add(split);
		
		WindowUtil.onClose(split, () ->{
			prefs.putInt("dividerLocation", split.getDividerLocation());
		});
		split.setDividerLocation(prefs.getInt("dividerLocation", -1));

		JTable table = new JTable();
		var tableScroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_ALWAYS,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		split.add(tableScroll);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		JTextArea output = new JTextArea();
		var scrollPane = new JScrollPane(output);
		split.add(scrollPane);
		output.enableInputMethods(false);
		output.setEditable(false);
		
//		ParagraphActions paragraphActions = new ParagraphActions(output);
		
		output.setCaret(new ParagraphCaret());

		setError = message -> {
			invokeLater(() -> {
				output.setForeground(Color.RED);
				output.setText(message);
			});
		};

		var defaultForeground = output.getForeground();
		setOutput = message -> {
			invokeLater(() -> {
				output.setForeground(defaultForeground);
				output.setText(message);
			});
		};
		
		Supplier<Optional<Long>> selection = () -> {
			return Optional.of(table.getSelectedRow())
					.filter(x -> x >= 0)
					.map(index -> 
			table.getModel().getValueAt(index, 0)).map(Long.class::cast);
		};
		
		String filterName = "filter";
		var filterAction = new AbstractAction(filterName) {
			private static final long serialVersionUID = 2783229494384910800L;
			@Override
			public void actionPerformed(ActionEvent e) {
				model.setFilter(filter.isSelected());
			}
		};
		filterAction.putValue(Action.SHORT_DESCRIPTION, "Disable jstackfilter to see original jstack output");
		filter.setAction(filterAction);
		
		
		String refreshName = "refresh";
		AbstractAction refreshAction = new AbstractAction(refreshName) {
			private static final long serialVersionUID = -5436279312088472338L;
			@Override
			public void actionPerformed(ActionEvent e) {
				Optional<Long> previousSelection = selection.get();
				table.setModel(toTableModel(model.getJavaProcesses()));
				previousSelection.ifPresent(selection -> 
					selectRowByFirstColumn(table, selection)
				);
				packColumns(table);
				Dimension size = new Dimension(100, table.getRowHeight() * table.getModel().getRowCount());
				table.setPreferredScrollableViewportSize(size);
			}

		};
		refreshAction.putValue(Action.SHORT_DESCRIPTION, "(F5) Reload the file, rerun jps and jstack.");
		frame.getRootPane().getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), refreshName);
		frame.getRootPane().getActionMap().put(refreshName, refreshAction);
		refreshButton.setAction(refreshAction);

		
		Clipboard clipboard = frame.getToolkit().getSystemClipboard();
		String pasteName = (String) TransferHandler.getPasteAction().getValue(Action.NAME);
		AbstractAction pasteAction = new AbstractAction(pasteName) {
			
			private static final long serialVersionUID = 9199450855113081882L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					table.clearSelection();
					model.setInput((String) clipboard.getData(DataFlavor.stringFlavor));
				} catch (UnsupportedFlavorException | IOException e1) {
					handleError(e1);
				}
			}
		};
		pasteAction.putValue(Action.SHORT_DESCRIPTION, "(Ctrl+V) Take jstack output from system's clipboard.");
		WindowUtil.handleKeystrokes(frame.getRootPane(), pasteName, KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_V, KeyEvent.META_DOWN_MASK));
		frame.getRootPane().getActionMap().put(pasteName, pasteAction);
		frame.getRootPane().getActionMap().put(DefaultEditorKit.pasteAction, pasteAction);
		table.getActionMap().put(pasteName, pasteAction);
		output.getActionMap().put(DefaultEditorKit.pasteAction, pasteAction);
		pasteButton.setAction(pasteAction);
		
		
		String loadName = "load";
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		String previousFile = prefs.get("lastFile", System.getProperty("user.home"));
		fileChooser.ensureFileIsVisible(new File(previousFile));
		AbstractAction loadAction = new AbstractAction(loadName) {
			private static final long serialVersionUID = 9199450855113081882L;
			@Override
			public void actionPerformed(ActionEvent e) {
				table.clearSelection();
				int result = fileChooser.showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
				    File selectedFile = fileChooser.getSelectedFile();
				    prefs.put("lastFile", selectedFile.toString());
				    model.setFile(selectedFile.toPath());
				}
			}
		};
		loadAction.putValue(Action.SHORT_DESCRIPTION, "(Ctrl+O) Load jstack output from a file.");
		WindowUtil.handleKeystrokes(frame.getRootPane(), loadName, KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_O, KeyEvent.META_DOWN_MASK));
		frame.getRootPane().getActionMap().put(loadName, loadAction);
		table.getActionMap().put(loadName, loadAction);
		loadButton.setAction(loadAction);
		
		var markIdleName = "mark idle";
		AbstractAction markIdle = new AbstractAction(markIdleName) {
			private static final long serialVersionUID = 9199450855113081882L;
			@Override
			public void actionPerformed(ActionEvent e) {
				String selectedText = output.getSelectedText();
				if (selectedText != null) {
					model.rememberIdleThreads(selectedText);
				}
			}
		};
		markIdle.putValue(Action.SHORT_DESCRIPTION, "(Ctrl+I) Mark selected threads as idle.");
		WindowUtil.handleKeystrokes(frame.getRootPane(), markIdleName, KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK), KeyStroke.getKeyStroke(KeyEvent.VK_I, KeyEvent.META_DOWN_MASK));
		frame.getRootPane().getActionMap().put(markIdleName, markIdle);
		markIdleButton.setAction(markIdle);

		String exitName = "exit";
		AbstractAction exitAction = new AbstractAction(exitName) {
			private static final long serialVersionUID = -5146316832581685550L;

			@Override
			public void actionPerformed(ActionEvent e) {
				frame.dispose();
			}
		};
		
		frame.getRootPane().getActionMap().put(exitName, exitAction);
		WindowUtil.handleKeystrokes(frame.getRootPane(), exitName, KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, 0));
		
		table.setAutoCreateColumnsFromModel(false);
		var column = new TableColumn(0);
		column.setHeaderValue("PID");;
		table.getColumnModel().addColumn(column);
		column = new TableColumn(1);
		column.setHeaderValue("Command");;
		table.getColumnModel().addColumn(column);



		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				selection.get().ifPresent(model::selectJavaProcess);
			}
		});

		tableScroll.setMinimumSize(new Dimension(200, 100));

		refreshAction.actionPerformed(null);
		frame.setVisible(true);
	}

	private static void packColumns(JTable table) {
		for (int column = 0; column < table.getColumnCount(); column++) {
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

			tableColumn.setPreferredWidth(preferredWidth);
		}
	}
	
	private static void selectRowByFirstColumn(JTable table, Object selection) {
		for (int row = 0; row < table.getRowCount(); row++) {
			if (Objects.equals(selection, table.getValueAt(row, 0))) {
				table.getSelectionModel().setSelectionInterval(row, row);
			}
		}
	}

	private static TableModel toTableModel(List<JavaProcess> rows) {
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