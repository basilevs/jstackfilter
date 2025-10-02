package org.basilevs.jstackfilter.ui;

import static javax.swing.KeyStroke.getKeyStroke;
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
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Stream;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;
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
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			throw new IllegalStateException(e); 
		}
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
		var showIdleThreads = new JCheckBox();
		controls.add(showIdleThreads);
		showIdleThreads.setSelected(false);
		
		var showIdenticalThreads = new JCheckBox();
		controls.add(showIdenticalThreads);
		showIdenticalThreads.setSelected(false);

		var showOldProcesses = new JCheckBox("Old processes");
		controls.add(showOldProcesses);
		showOldProcesses.setSelected(true);

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
		
		List<Action> actions = new ArrayList<>();
		actions.add(configureCheckbox(showIdleThreads, "Idle", "Do not filter out idle threads, show original jstack output", isSelected -> {
			model.showIdle(isSelected);
		}));

		actions.add(configureCheckbox(showIdenticalThreads, "Identical", "Do not filter out similar threads, show original jstack output", isSelected -> {
			model.showIdentical(isSelected);
		}));

		actions.add(configureCheckbox(showOldProcesses, "Old processes", "When unchecked, currently runnning processes are hidden. Refresh action would only show processes created since.", isSelected -> {
			model.showOldProcesses(isSelected);
			refreshButton.doClick();
		}));


		AbstractAction refreshAction = createAction("Refresh", "(F5) Reload the file, rerun jps and jstack.", KeyStroke.getKeyStroke(KeyEvent.VK_F5, 0), () -> {
			Optional<Long> previousSelection = getSelection(table);
			table.setModel(toTableModel(model.getJavaProcesses()));
			previousSelection.ifPresent(selection -> 
				selectRowByFirstColumn(table, selection)
			);
			packColumns(table);
			Dimension size = new Dimension(100, table.getRowHeight() * table.getModel().getRowCount());
			table.setPreferredScrollableViewportSize(size);
			if (!previousSelection.isPresent()) {
				model.refresh();
			}
		});
		actions.add(refreshAction);
		refreshButton.setAction(refreshAction);

		
		Clipboard clipboard = frame.getToolkit().getSystemClipboard();
		String pasteName = (String) TransferHandler.getPasteAction().getValue(Action.NAME);
		AbstractAction pasteAction = createAction(pasteName, "(Ctrl+V) Take jstack output from system's clipboard.", getKeyStroke(KeyEvent.VK_V, KeyEvent.CTRL_DOWN_MASK), () -> {
			try {
				table.clearSelection();
				model.setInput((String) clipboard.getData(DataFlavor.stringFlavor));
			} catch (UnsupportedFlavorException | IOException e1) {
				handleError(e1);
			}
		});
		actions.add(pasteAction);
		frame.getRootPane().getActionMap().put(DefaultEditorKit.pasteAction, pasteAction);
		output.getActionMap().put(DefaultEditorKit.pasteAction, pasteAction);
		pasteButton.setAction(pasteAction);
		
		
		if (fileChooser == null) {
			fileChooser = new JFileChooser();
			fileChooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
		}
		File previousFile = new File(prefs.get("lastFile", System.getProperty("user.home")));
		fileChooser.ensureFileIsVisible(previousFile);
		fileChooser.setCurrentDirectory(previousFile);
		AbstractAction loadAction = createAction("load", "(Ctrl+O) Load jstack output from a file.", getKeyStroke(KeyEvent.VK_O, KeyEvent.CTRL_DOWN_MASK), () -> {
				table.clearSelection();
				int result = fileChooser.showOpenDialog(frame);
				if (result == JFileChooser.APPROVE_OPTION) {
				    File selectedFile = fileChooser.getSelectedFile();
				    prefs.put("lastFile", selectedFile.toString());
				    model.setFile(selectedFile.toPath());
				}
		});
		actions.add(loadAction);
		loadButton.setAction(loadAction);
		
		AbstractAction markIdle = createAction("mark idle", "(Ctrl+I) Mark selected threads as idle.", getKeyStroke(KeyEvent.VK_I, KeyEvent.CTRL_DOWN_MASK), () -> {
				String selectedText = output.getSelectedText();
				if (selectedText != null) {
					model.rememberIdleThreads(selectedText);
				}
		});
		actions.add(markIdle);
		markIdleButton.setAction(markIdle);

		AbstractAction exitAction = createAction("exit", "(Esc) Exit Jstackfilter", getKeyStroke(KeyEvent.VK_ESCAPE, 0), () -> {
			frame.dispose();
		});
		actions.add(exitAction);
		
		for (Action a: actions) {
			KeyStroke ks =  (KeyStroke) a.getValue(Action.ACCELERATOR_KEY);
			KeyStroke[] keys = alternateKeys(ks).toArray(KeyStroke[]::new);
			Stream.of(Action.ACTION_COMMAND_KEY, Action.NAME).map(a::getValue).filter(Objects::nonNull).map(String.class::cast).forEach(command -> {
				frame.getRootPane().getActionMap().put(command, a);
				table.getActionMap().put(command, a); // suppress default actions
				output.getActionMap().put(command, a); // suppress default actions
				WindowUtil.handleKeystrokes(frame.getRootPane(), command, keys);
			});
		}
		
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
				getSelection(table).ifPresent(model::selectJavaProcess);
			}
		});

		tableScroll.setMinimumSize(new Dimension(200, 100));

		refreshAction.actionPerformed(null);
		frame.setVisible(true);
	}

	private static Stream<KeyStroke> alternateKeys(KeyStroke origin) {
		if (origin == null) {
			return Stream.empty();
		}
		KeyStroke alternative = null;
		int modifiers = origin.getModifiers();
		if ((modifiers & KeyEvent.CTRL_DOWN_MASK) !=0) {
			alternative = getKeyStroke(origin.getKeyCode(), KeyEvent.META_DOWN_MASK);
		}
		return Stream.of(origin, alternative).filter(Objects::nonNull);
	}

	private AbstractAction createAction(String name, String description, KeyStroke accelerator, Runnable r) {
		AbstractAction refreshAction = new AbstractAction(name) {
			private static final long serialVersionUID = -5436279312088472338L;
			@Override
			public void actionPerformed(ActionEvent e) {
				r.run();
			}
		};
		refreshAction.putValue(Action.SHORT_DESCRIPTION, description);
		refreshAction.putValue(Action.ACCELERATOR_KEY, accelerator);
		configureMnemonic(refreshAction);
		return refreshAction;
	}

	private AbstractAction configureCheckbox(JCheckBox checkbox, String text, String description, Consumer<Boolean> onChange) {
		AbstractAction action = createAction(text, description, null,() -> {
				onChange.accept(checkbox.isSelected());
		});
		checkbox.setAction(action);
		action.actionPerformed(null);
		return action;
	}

	private final Set<String> usedMnemonics = new HashSet<>();
	private void configureMnemonic(AbstractAction action) {
		for (char c: ((String)action.getValue(Action.NAME)).toCharArray()) {
			c = Character.toLowerCase(c);
			int offset = c - 'a';
			int code = KeyEvent.VK_A + offset;
			String mnemonic = String.valueOf(c);
			if (usedMnemonics.add(mnemonic)) {
				action.putValue(Action.MNEMONIC_KEY, code);
				break;
			}
		}
	}

	private Optional<Long> getSelection(JTable processTable) {
		return Optional.of(processTable.getSelectedRow())
				.filter(x -> x >= 0)
				.map(index -> 
		{
			return processTable.getModel().getValueAt(index, 0);
		}).map(Long.class::cast);
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