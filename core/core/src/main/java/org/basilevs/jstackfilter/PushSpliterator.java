package org.basilevs.jstackfilter;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

public class PushSpliterator<T> implements Spliterator<T>, Closeable {

	public static <T> Stream<T> parallel(Stream<T> input) {
		Spliterator<T> inputSpliterator = input.spliterator();
		PushSpliterator<T> spliterator = new PushSpliterator<>() {
			@Override
			public long estimateSize() {
				long result = inputSpliterator.estimateSize() + super.estimateSize();
				if (result < 0) {
					result = Long.MAX_VALUE;
				}
				return result;
			}

			@Override
			public int characteristics() {
				return super.characteristics() | inputSpliterator.characteristics() & ~SORTED & ~SIZED & ~ORDERED;
			}
		};
		CompletableFuture<Void> reader = CompletableFuture.runAsync(() -> {
			try {
				try {
					boolean[] stop = new boolean[] { false };
					while (!stop[0]) {
						if (!inputSpliterator.tryAdvance(t -> {
							try {
								spliterator.put(t);
							} catch (InterruptedException e) {
								Thread.currentThread().interrupt();
								stop[0] = true;
							}
						})) {
							break;
						}
					}
				} finally {
					spliterator.done();
				}
			} catch (InterruptedException e) {
				spliterator.close();
			}
		});
		return StreamSupport.stream(spliterator, true).onClose(() -> {
			try {
				reader.cancel(true);
			} finally {
				try {
					input.close();
				} finally {
					spliterator.close();
				}
			}
		});
	}

	public final void put(T item) throws InterruptedException {
		pipe.put(new Some(item));
	}

	public final void done() throws InterruptedException {
		pipe.put(end);
	}

	@Override
	public final boolean tryAdvance(Consumer<? super T> action) {
		try {
			Entry<T> item = pipe.take();
			return item.tryAdvance(action);
		} catch (InterruptedException e) {
			close();
			Thread.currentThread().interrupt();
			return false;
		}
	}

	@Override
	public final Spliterator<T> trySplit() {
		Collection<Entry<T>> chunk = new ArrayList<>();
		pipe.drainTo(chunk);
		if (chunk.removeIf(Entry::isDone)) {
			close();
		}
		if (chunk.isEmpty()) {
			return null;
		}
		return chunk.stream().flatMap(Entry::stream).spliterator();
	}

	@Override
	public long estimateSize() {
		return pipe.size();
	}

	@Override
	public int characteristics() {
		return SUBSIZED;
	}

	@Override
	public final void close() {
		do {
			pipe.clear();
		} while (!pipe.offer(end));
	}

	private interface Entry<T> {
		boolean tryAdvance(Consumer<? super T> action);

		Stream<T> stream();

		boolean isDone();
	}

	private final class End implements Entry<T> {
		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			close();
			return false;
		}

		@Override
		public Stream<T> stream() {
			return Stream.empty();
		}

		@Override
		public boolean isDone() {
			return true;
		}
	}

	private final class Some implements Entry<T> {
		public final T item;

		public Some(T item) {
			this.item = item;
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			action.accept(item);
			return true;
		}

		@Override
		public Stream<T> stream() {
			return Stream.of(item);
		}

		@Override
		public boolean isDone() {
			return false;
		}
	}

	// Use of Object collection instead of Entry wrappers reduces performance
	private final ArrayBlockingQueue<Entry<T>> pipe = new ArrayBlockingQueue<>(1000);
	private final End end = new End();
}
