package org.basilevs.jstackfilter.ui.internal;

import java.awt.event.ActionEvent;
import java.lang.System.Logger;
import java.lang.System.Logger.Level;
import java.util.function.BiFunction;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.text.DefaultCaret;
import javax.swing.text.JTextComponent;
import javax.swing.text.Position.Bias;

import org.basilevs.jstackfilter.ui.internal.Paragraphs.Range;

public class ParagraphCaret extends DefaultCaret {
	private static final long serialVersionUID = -3113229730163357205L;
	private static final Logger LOG = System.getLogger(ParagraphCaret.class.getName());

	@Override
	public void install(JTextComponent c) {
		super.install(c);
		addAction("caret-up", Paragraphs::findPreviousParagraphRange);
		addAction("caret-down", Paragraphs::findNextParagraphRange);
		addAction("selection-up", Paragraphs::includePreviousParagraph);
		addAction("selection-down", Paragraphs::includeNextParagraph);
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

	private void addAction(String name, BiFunction<String, Range, Range> transformRange) {
		getComponent().getActionMap().put(name, new AbstractAction(name) {
			private static final long serialVersionUID = 9093329936068012782L;
			@Override
			public final void actionPerformed(ActionEvent e) {
				Range range = transformRange.apply(getComponent().getText(), new Range(getMark(), getDot()));
				LOG.log(Level.DEBUG, getValue(Action.NAME) + ": {0} {1} {2}", getMark(), getDot(), range);
				ParagraphCaret.super.setDot(range.from, Bias.Forward);
				ParagraphCaret.super.moveDot(range.to, Bias.Forward);

			}
		});
	}
}
