package org.basilevs.jstackfilter.ui.internal;

public class Paragraphs {
	private static final String PARAGRAPH_DELIMITER = "\n\n";
	private Paragraphs() {}
	
	public static class Range {
		public final int from, to;

		public Range(int from, int to) {
			this.from = from;
			this.to = to;
		}
		
		public Range inverse() {
			return new Range(to, from);
		}
		
		@Override
		public String toString() {
			return "Range [from=" + from + ", to=" + to + "]";
		}

		public int min() {
			return Math.min(to, from);
		}
		
		public int max() {
			return Math.max(to, from);
		}
	}
	
	public static Range toParagraphRange(String text, int from, int to) {
		int start = Paragraphs.findParagraphStart(text, Math.min(from, to));
		int end = Paragraphs.findParagraphEnd(text, Math.max(from,  to));

		Range result ;
		
		int length = PARAGRAPH_DELIMITER.length();
		int step = length + 1;
		if (start > end) {
			if (end + PARAGRAPH_DELIMITER.length() + 1 < text.length()) {
				result = toParagraphRange(text, end + length, end + step);
			} else if (start - step > 0) {
				result = toParagraphRange(text, start - length, start - step);
			} else {
				result = new Range(0, text.length());
			}
		} else {
			result = new Range(start, end);
		}
		
		if (from > to) {
			result = result.inverse();
		}
		return result;
		
	}

	public static int findParagraphStart(String text, int position) {
		int start = text.lastIndexOf(PARAGRAPH_DELIMITER, position);
		if (start < 0) {
			start = 0;
		} else {
			start += PARAGRAPH_DELIMITER.length();
		}
		return start;
	}

	private static int findParagraphEnd(String text, int position) {
		int end = text.indexOf(PARAGRAPH_DELIMITER, position);
		if (end == -1) {
			end = text.length();
		}
		return end;
	}

	public static Range findPreviousParagraphRange(String text, Range before) {
		int newPos = before.min() - PARAGRAPH_DELIMITER.length() * 2;
		return toParagraphRange(text, newPos, newPos - 1);
	}

	public static Range findNextParagraphRange(String text, Range before) {
		int newPos = before.max() + PARAGRAPH_DELIMITER.length() * 2;
		return toParagraphRange(text, newPos, newPos + 1);
	}

	public static Range includePreviousParagraph(String text, Range before) {
		Range previous = findPreviousParagraphRange(text, before);
		return new Range(before.max(), previous.to);
	}

	public static Range includeNextParagraph(String text, Range before) {
		Range next = findNextParagraphRange(text, before);
		return new Range(before.min(), next.to);
	}

}
