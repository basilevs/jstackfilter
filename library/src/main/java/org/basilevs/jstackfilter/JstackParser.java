package org.basilevs.jstackfilter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.Optional;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.grammar.jstack.ParseException;
import org.basilevs.jstackfilter.grammar.jstack.TokenMgrError;

public class JstackParser {

	/** Parses threads as printed by jstack. Header and native threads are ignored.  */
	public static Stream<JavaThread> parseThreads(Reader reader) {
		return splitToChunks(reader).map(JstackParser::parseThread).flatMap(Optional::stream);
	}
	
	public static Stream<String> splitToChunks(Reader reader) {
		Scanner scanner = new Scanner(reader);
		scanner.useDelimiter("(?:\n\r|\r\n|\n){2}");
		return scanner.tokens().onClose(() -> {
			scanner.close();
			IOException error = scanner.ioException();
			if (error != null) {
				throw new IllegalStateException(error);
			}
		});
	}
	
	
	private static final Pattern JAVA_THREAD_HEADER_PATTERN = Pattern.compile("^\"[^\\n]+\" #\\d");
	public static Optional<JavaThread> parseThread(String thread) {
		Matcher matcher = JAVA_THREAD_HEADER_PATTERN.matcher(thread);
		if (!matcher.find() || matcher.start() != 0) {
			return Optional.empty();
		}
		String terminatedThread = thread+"\n";
		var parser = new org.basilevs.jstackfilter.grammar.jstack.JstackParser(new StringReader(terminatedThread));
		try {
			return Optional.of(parser.javaThread().withRepresentation(thread));
		} catch (ParseException e) {
			var lines = thread.split("\n");
			StringBuilder message = new StringBuilder("Can't parse Java thread:\n");
			for (int i = e.currentToken.beginLine; i <= e.currentToken.endLine; i++) {
				message.append(" > ");
				message.append(lines[i-1]);
				message.append("\n");
			}
			message.append(terminatedThread);
			
			throw new IllegalArgumentException(message.toString(), e);
		} catch (TokenMgrError e) {
			throw new IllegalArgumentException(e.getMessage() + ":\n" + terminatedThread, e);
		}
	}

}
