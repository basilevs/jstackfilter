package org.basilevs.jstackfilter.ui.internal;

public class Paragraphs {
	private static final String PARAGRPAH_DELIMIETER = "\n\n";
	private Paragraphs() {}
	
	public static class Range {
		public final int from, to;

		public Range(int from, int to) {
			super();
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
	}
	
	public static Range toParagraphRange(String text, int from, int to) {
		int start = Paragraphs.findParagraphStart(text, Math.min(from, to));
		int end = Paragraphs.findParagraphEnd(text, Math.max(from,  to));

		Range result ;
		
		int length = PARAGRPAH_DELIMIETER.length();
		int step = length + 1;
		if (start > end) {
			if (end + PARAGRPAH_DELIMIETER.length() + 1 < text.length()) {
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
		int start = text.lastIndexOf(PARAGRPAH_DELIMIETER, position);
		if (start < 0) {
			start = 0;
		} else {
			start += PARAGRPAH_DELIMIETER.length();
		}
		return start;
	}

	private static int findParagraphEnd(String text, int position) {
		int end = text.indexOf(PARAGRPAH_DELIMIETER, position);
		if (end == -1) {
			end = text.length();
		}
		return end;
	}

	public static Range findPreviousParagraphRange(String text, int from, int to) {
		int newPos = Math.min(from, to) - PARAGRPAH_DELIMIETER.length() * 2;
		return toParagraphRange(text, newPos, newPos - 1);
	}

	public static Range findNextParagraphRange(String text, int from, int to) {
		int newPos = Math.max(from, to) + PARAGRPAH_DELIMIETER.length() * 2;
		return toParagraphRange(text, newPos, newPos + 1);
	}

}
