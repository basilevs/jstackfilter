package org.basilevs.jstackfilter;

import java.io.Closeable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/** Speed up serial inputs by dedicating a thread to them
 * 
 *  **/
public class PushSpliterator<T> implements Spliterator<T>, Closeable {
	public PushSpliterator(int queueCapacity, int splitThreshold) {
		this.pipe = new ArrayBlockingQueue<>(queueCapacity);
		if (splitThreshold <= 0) {
			throw new IllegalArgumentException("expected splitThreshold > 0, but received - " + splitThreshold);
		}
		this.splitThreshold = splitThreshold;
	}

	public static final class ClosedException extends Exception {
		private static final long serialVersionUID = 4707636972101757900L;
	}

	public static <T> Stream<T> parallel(Stream<T> input, int queueCapacity, int splitThreshold) {
		Spliterator<T> inputSpliterator = input.spliterator();
		PushSpliterator<T> spliterator = new PushSpliterator<>(queueCapacity, splitThreshold) {
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
				while (!spliterator.isClosed() && inputSpliterator.tryAdvance(t -> {
					try {
						spliterator.put(t);
					} catch (InterruptedException e) {
						Thread.currentThread().interrupt();
						spliterator.close();
					} catch (ClosedException e) {
						// The loop will terminate by checking spliterator.isClosed()
					}
				})) {
				}
				if (!spliterator.isClosed()) {
					spliterator.done();
				}
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				spliterator.close();
			} catch (Throwable e) {
				spliterator.close();
				throw e;
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
			if (!reader.isCancelled()) {
				// Throw if reader terminated unexpectedly
				reader.join();
			}
		});
	}

	public boolean isClosed() {
		return closed.get();
	}

	public final void put(T item) throws InterruptedException, ClosedException {
		ensureOpen();
		pipe.put(new Some(item));
		ensureOpen();
	}

	public final void done() throws InterruptedException {
		// Do not block on queue, when consumers may not longer be active
		if (isClosed()) {
			close();
		} else {
			pipe.put(end);
		}
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
		if (pipe.size() < splitThreshold) {
			return null;
		}
		Collection<Entry<T>> chunk = new ArrayList<>();
		pipe.drainTo(chunk);
		if (chunk.removeIf(Entry::isDone)) {
			close();
		}
		if (chunk.isEmpty()) {
			return null;
		}
		return chunk.stream().flatMap(Entry::stream).parallel().spliterator();
	}

	@Override
	public long estimateSize() {
		return pipe.size();
	}

	@Override
	public int characteristics() {
		return SUBSIZED | CONCURRENT;
	}

	@Override
	public final void close() {
		closed.set(true);
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

	private void ensureOpen() throws ClosedException {
		if (isClosed()) {
			close(); // Do not allow queue to accumulate garbage data
			throw new ClosedException();
		}
	}

	private final AtomicBoolean closed = new AtomicBoolean(false);
	// Raw Object references and "instanceof" are slower than Entry wrapping
	private final ArrayBlockingQueue<Entry<T>> pipe;
	private final End end = new End();
	private int splitThreshold;

}
