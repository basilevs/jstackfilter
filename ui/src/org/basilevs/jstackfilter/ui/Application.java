package org.basilevs.jstackfilter.ui;

import static javax.swing.SwingUtilities.invokeLater;

import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.List;
import java.util.function.Consumer;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Box;
import javax.swing.JButton;
import javax.swing.JCheckBox;
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

import org.basilevs.jstackfilter.ui.internal.WindowUtil;

public class Application {
	private final Preferences prefs = Preferences.userNodeForPackage(Application.class);
	private Consumer<String> setError;
	private Consumer<String> setOutput;
	private final Model model = new Model(this::handleError, this::handleOutput);

	private Application() throws BackingStoreException {
	}

	private void handleError(String message) {
		setError.accept(message);
	}

	private void handleOutput(String output) {
		setOutput.accept(output);
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
				model.close();
			}
		});

		Container content = frame.getContentPane();
		content.setLayout(new GridBagLayout());

		var controls = Box.createHorizontalBox();
		var c = new GridBagConstraints();
		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.weightx = 1;
		content.add(controls, c);
		var filter = new JCheckBox("Filter");
		controls.add(filter);
		filter.setSelected(true);
		filter.addItemListener(new ItemListener() {

			@Override
			public void itemStateChanged(ItemEvent e) {
				model.setFilter(filter.isSelected());
			}
		});
		
		var refreshButton = new JButton("Refresh");
		controls.add(refreshButton);

		JTable table = new JTable();
		var tableScroll = new JScrollPane(table, JScrollPane.VERTICAL_SCROLLBAR_NEVER,
				JScrollPane.HORIZONTAL_SCROLLBAR_ALWAYS);
		content.add(tableScroll, c);
		table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);

		JTextArea output = new JTextArea();
		var scrollPane = new JScrollPane(output);
		c.fill = GridBagConstraints.BOTH;
		c.weighty = 1;
		content.add(scrollPane, c);

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
		
		AbstractAction refreshAction = new AbstractAction("Refresh") {

			private static final long serialVersionUID = -5436279312088472338L;

			@Override
			public void actionPerformed(ActionEvent e) {
				table.setModel(toTableModel(model.getJavaProcesses()));
				packColumns(table);
				Dimension size = new Dimension(100, table.getRowHeight() * table.getModel().getRowCount());
				table.setPreferredScrollableViewportSize(size);
			}
			
		};
		
		table.setAutoCreateColumnsFromModel(false);
		refreshButton.setAction(refreshAction);
		var column = new TableColumn(0);
		column.setHeaderValue("PID");;
		table.getColumnModel().addColumn(column);
		column = new TableColumn(1);
		column.setHeaderValue("Command");;
		table.getColumnModel().addColumn(column);
		refreshAction.actionPerformed(null);



		table.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
			@Override
			public void valueChanged(ListSelectionEvent e) {
				int index = table.getSelectedRow();
				if (index >= 0) {
					model.selectRow(table.getModel().getValueAt(index, 0));
				}
			}
		});

		tableScroll.setMinimumSize(new Dimension(200, 100));

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