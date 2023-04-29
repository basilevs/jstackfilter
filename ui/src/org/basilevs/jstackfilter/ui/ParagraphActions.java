package org.basilevs.jstackfilter.ui;

import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Objects;

import javax.swing.AbstractAction;
import javax.swing.JPopupMenu;
import javax.swing.JTextArea;
import javax.swing.SwingUtilities;
import javax.swing.text.JTextComponent;

public class ParagraphActions {
	private final JTextComponent textArea;
	private final JPopupMenu menu;
	private final String PARAGRPAH_DELIMIETER = "\n\n";

	public ParagraphActions(JTextArea textArea) {
		this.textArea = Objects.requireNonNull(textArea);
		this.menu = new JPopupMenu();
		textArea.addMouseListener(new MouseAdapter() {
			public void mouseReleased(MouseEvent e) {
				if (e.isPopupTrigger()) {
					menu.show(e.getComponent(), e.getX(), e.getY());
				}
			}
		});
		textArea.addCaretListener(e -> {
			int targetStart = textArea.getSelectionStart();
			int targetEng = textArea.getSelectionEnd();
			selectParagraphs(targetStart, targetEng, false);
		});

		// Handle caret-up action
		textArea.getActionMap().put("caret-up", new AbstractAction("caret-up") {
			private static final long serialVersionUID = -8756816970079307265L;

			@Override
			public void actionPerformed(ActionEvent e) {
				int position = textArea.getSelectionStart() - (PARAGRPAH_DELIMIETER.length() * 2);
				selectParagraphs(position, position, true);
			}
		});
	}

	private void selectParagraphs(int targetStart, int targetEng, boolean scrollBack) {
		int start = findParagraphStart(targetStart);
		int end = findParagraphEnd(targetEng);

		if (start < end && !inUpdate) {
			int caret;
			int mark;
			if (scrollBack) {
				caret = start;
				mark = end;
			} else {
				mark = start;
				caret = end;
			}
			SwingUtilities.invokeLater(() -> {
				inUpdate = true;
				try {
					textArea.setCaretPosition(mark);
					textArea.moveCaretPosition(caret);
				} finally {
					inUpdate = false;
				}
			});
		}
	}

	private boolean inUpdate = false;

	private int findParagraphStart(int min) {
		int start = textArea.getText().lastIndexOf(PARAGRPAH_DELIMIETER, min);
		if (start == -1) {
			start = 0;
		} else {
			start += PARAGRPAH_DELIMIETER.length();
		}
		return start;
	}

	private int findParagraphEnd(int max) {
		String text = textArea.getText();
		int end = text.indexOf(PARAGRPAH_DELIMIETER, max);
		if (end == -1) {
			end = text.length();
		}
		return end;
	}

}
