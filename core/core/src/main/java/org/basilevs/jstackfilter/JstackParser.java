package org.basilevs.jstackfilter;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.Optional;
import java.util.Queue;
import java.util.Scanner;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.basilevs.jstackfilter.grammar.jstack.ParseException;
import org.basilevs.jstackfilter.grammar.jstack.TokenMgrError;

public class JstackParser {

	/**
	 * Parses threads as printed by jstack. Header and native threads are ignored.
	 */
	public static Stream<JavaThread> parseThreads(Reader reader) {
		return parallel(splitToChunks(reader)).map(JstackParser::parseThread).flatMap(Optional::stream);
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
	private interface Entry<T> {
		boolean tryAdvance(Queue<Entry<T>> queue, Consumer<? super T> action);
		Stream<T> stream();
	}
	
	private static final class End<T> implements Entry<T> {
		public static End<?> INSTANCE = new End<>();
		@SuppressWarnings("unchecked")
		public static <T> End<T> getInstance() {
			return (End<T>) INSTANCE;
		}
		@Override
		public boolean tryAdvance(Queue<Entry<T>> queue, Consumer<? super T> action) {
			queue.clear();
			queue.offer(getInstance());
			return false;
		}
		@Override
		public Stream<T> stream() {
			return Stream.empty();
		}
	}
	private static final class Some<T> implements Entry<T> {
		public final T item;
		public Some(T item) {
			this.item = item;
		}
		@Override
		public boolean tryAdvance(Queue<Entry<T>> queue, Consumer<? super T> action) {
			action.accept(item);
			return true;
		}
		@Override
		public Stream<T> stream() {
			return Stream.of(item);
		}
	}
	public static <T> Stream<T> parallel(Stream<T> input) {
		ArrayBlockingQueue<Entry<T>> pipe = new ArrayBlockingQueue<>(10000);
		CompletableFuture<Void> reader = CompletableFuture.runAsync(() -> {
			try {
				try {
					for (Iterator<T> i = input.iterator(); i.hasNext(); ) {
						pipe.put(new Some<>(i.next()));
					}
				} finally {
					pipe.put(End.getInstance());
				}
		    } catch (InterruptedException e) {
		    	pipe.clear();
		    	pipe.offer(End.getInstance());
		    }
		});
		Spliterator<T> spliterator = new Spliterator<>() {

			@Override
			public boolean tryAdvance(Consumer<? super T> action) {
				try {
					return pipe.take().tryAdvance(pipe, action);
				} catch (InterruptedException e) {
			    	pipe.clear();
			    	pipe.offer(End.getInstance());
					return false;
				}
			}

			@Override
			public Spliterator<T> trySplit() {
				Collection<Entry<T>> chunk = new ArrayList<>();
				int result = pipe.drainTo(chunk);
				if (result == 0) {
					return null;
				}
				if (chunk.removeAll(Collections.singleton(End.getInstance()))) {
					pipe.clear();
					pipe.offer(End.getInstance());
				}
				if (chunk.isEmpty()) {
					return null;
				}
				return chunk.stream().flatMap(Entry::stream).spliterator();
			}

			@Override
			public long estimateSize() {
				return Long.MAX_VALUE;
			}

			@Override
			public int characteristics() {
				return CONCURRENT | SUBSIZED;
			}
			
		};
		return StreamSupport.stream(spliterator, true).onClose(() -> {
			try {
				reader.cancel(true);
			} finally {
				try {
					input.close();
				} finally {
					pipe.clear();
					pipe.offer(End.getInstance());
				}
			} 
		});
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
