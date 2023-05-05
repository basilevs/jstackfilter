package org.basilevs.jstackfilter.ui.internal;

import java.awt.event.ActionEvent;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position.Bias;

import org.basilevs.jstackfilter.ui.internal.Paragraphs.Range;

public class ParagraphCaret extends DefaultCaret {
	private static final long serialVersionUID = -3113229730163357205L;
	private static final Logger LOG = System.getLogger(ParagraphCaret.class.getName());

	private abstract class RangeChangeAction extends AbstractAction {
		private static final long serialVersionUID = 3515558612059027621L;

		public RangeChangeAction(String name) {
			super(name);
		}

		@Override
		public final void actionPerformed(ActionEvent e) {
			Range range = newRange(getComponent().getText(), getMark(), getDot());
			LOG.log(Level.DEBUG, getValue(Action.NAME) + ": {0} {1} {2}", getMark(), getDot(), range);
			ParagraphCaret.super.setDot(range.from, Bias.Forward);
			ParagraphCaret.super.moveDot(range.to, Bias.Forward);

		}

		protected abstract Range newRange(String text, int mark, int dot);
	}

	@Override
	public void install(JTextComponent c) {
		super.install(c);
		c.getActionMap().put("caret-up", new RangeChangeAction("caret-up") {
			private static final long serialVersionUID = 3515558612059027621L;

			@Override
			public Range newRange(String text, int mark, int dot) {
				return Paragraphs.findPreviousParagraphRange(text, mark, dot);
			}
		});

		c.getActionMap().put("caret-down", new RangeChangeAction("caret-down") {
			private static final long serialVersionUID = 6732567272487548873L;
			@Override
			public Range newRange(String text, int mark, int dot) {
				return Paragraphs.findNextParagraphRange(text, mark, dot);
			}
		});

		c.getActionMap().put("selection-up", new RangeChangeAction("selection-up") {
			private static final long serialVersionUID = 6004582118340973608L;
			@Override
			public Range newRange(String text, int mark, int dot) {
				return Paragraphs.includePreviousParagraph(text, mark, dot);
			}
		});

		c.getActionMap().put("selection-down", new RangeChangeAction("selection-down") {
			private static final long serialVersionUID = 9093329936068012782L;
			@Override
			public Range newRange(String text, int mark, int dot) {
				return Paragraphs.includeNextParagraph(text, mark, dot);
			}
		});

	}

	@Override
	public void moveDot(int dot, Bias dotBias) {
		Range range = Paragraphs.toParagraphRange(getComponent().getText(), getMark(), dot);
		LOG.log(Level.DEBUG, "moveDot: mark: {0}, old dot:{1}, new dot: {2}, {3}", getMark(), getDot(), dot, range);
		super.moveDot(range.to, dotBias);//
	}

	@Override
	public void setDot(int dot, Bias dotBias) {
		Range range = Paragraphs.toParagraphRange(getComponent().getText(), dot, dot);
		LOG.log(Level.DEBUG, "setDot: {0} {1} {2}", getMark(), dot, range);
		super.setDot(range.from, dotBias);
		super.moveDot(range.to, dotBias);
	}

}
