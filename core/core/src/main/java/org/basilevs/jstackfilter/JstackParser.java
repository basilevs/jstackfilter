package org.basilevs.jstackfilter;

import static org.basilevs.jstackfilter.PushSpliterator.parallel;

import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.grammar.jstack.ParseException;
import org.basilevs.jstackfilter.grammar.jstack.TokenMgrError;

public class JstackParser {

	/**
	 * Parses threads as printed by jstack. Header and native threads are ignored.
	 */
	public static Stream<JavaThread> parseThreads(Reader reader) {
		return PushSpliterator.parallel(FastChunkSplitter.splitToChunks(reader)).map(JstackParser::parseThread).flatMap(Optional::stream);
	}

	private static final Pattern JAVA_THREAD_HEADER_PATTERN = Pattern.compile("^\"[^\\n]+\" #\\d");

	public static Optional<JavaThread> parseThread(String thread) {
		Matcher matcher = JAVA_THREAD_HEADER_PATTERN.matcher(thread);
		if (!matcher.find() || matcher.start() != 0) {
			return Optional.empty();
		}
		
		// HACK: Remove extra quotes from thread name like:
		// "Worker-450: Resolving "Eclipse 2023-06 4.28" target definition" #6267 prio=5 os_prio=31 cpu=15827.54ms elapsed=16439.47s tid=0x000000010feba600 nid=0x3d05b in Object.wait()  [0x000000030316c000]
		// Ideally, this should be handled by grammar
		int end = thread.lastIndexOf('\"', matcher.end());
		String terminatedThread = removeQuotes(thread, matcher.start() + 1, end) + "\n";
		var parser = new org.basilevs.jstackfilter.grammar.jstack.JstackParser(new StringReader(terminatedThread));
		try {
			return Optional.of(parser.javaThread().withRepresentation(thread));
		} catch (ParseException e) {
			var lines = thread.split("\n");
			StringBuilder message = new StringBuilder("Can't parse Java thread:\n");
			for (int i = e.currentToken.beginLine; i <= e.currentToken.endLine; i++) {
				message.append(" > ");
				message.append(lines[i - 1]);
				message.append("\n");
			}
			message.append(terminatedThread);

			throw new IllegalArgumentException(message.toString(), e);
		} catch (TokenMgrError e) {
			throw new IllegalArgumentException(e.getMessage() + ":\n" + terminatedThread, e);
		}
	}

	private static String removeQuotes(String thread, int start, int end) {
		String prefix = thread.substring(0, start);
		String suffix = thread.substring(end);
		String target = thread.substring(start, end);
		return prefix + target.replace("\"", "") + suffix;
	}

}
