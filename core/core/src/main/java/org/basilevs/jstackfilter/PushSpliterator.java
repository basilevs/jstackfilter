package org.basilevs.jstackfilter;

import java.io.Closeable;
import java.lang.System.Logger.Level;
import java.util.Spliterator;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.ForkJoinWorkerThread;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.function.Consumer;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

/**
 * Speed up serial inputs by dedicating a thread to them. This is only effective
 * for slow inputs or large elements.
 * 
 * Rationale: java.nio.file.Files.lines(Path) and Scanner read content in
 * batches of 10000 elements, until a batch is loaded, processing does not start
 * The idea is to start processing early
 * 
 * It does not work for quickly processed elements, because of large
 * synchronization overheads.
 **/
public abstract class PushSpliterator<T> implements Spliterator<T> , Closeable {

	private static final Level LOG_LEVEL = System.Logger.Level.DEBUG;
	private static final System.Logger LOG = System.getLogger(PushSpliterator.class.getName());

	public PushSpliterator() {
		this.pipe = new ArrayBlockingQueue<>(getParallelism()*100);
		this.presplit = new PreSplitSpliterator<>(Stream.generate(Drainer::new).limit(getParallelism()).toList());
	}

	public static final class ClosedException extends Exception {
		private static final long serialVersionUID = 4707636972101757900L;
	}

	public static <T> Stream<T> parallel(Stream<T> input) {
		Spliterator<T> inputSpliterator = input.spliterator();
		PushSpliterator<T> spliterator = new PushSpliterator<>() {
			@Override
			public long estimateSize() {
				return inputSpliterator.estimateSize();
			}
			
			@Override
			public int characteristics() {
				return inputSpliterator.characteristics() & ~ORDERED & ~SORTED | CONCURRENT;
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
		if (!isClosed()) {
			pipe.put(end);
		}
	}

	@Override
	public final void close() {
		if (closed.compareAndSet(false, true)) {
			do {
				pipe.clear();
			} while (!pipe.offer(end));
		}
	}

	@Override
	public final boolean tryAdvance(Consumer<? super T> action) {
		return presplit.tryAdvance(action);
	}

	@Override
	public final Spliterator<T> trySplit() {
		return presplit.trySplit();
	}

	private interface Entry<T> {
		boolean tryAdvance(Consumer<? super T> action);
	}

	private final class End implements Entry<T> {
		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			LOG.log(LOG_LEVEL, "Disposing consumer");
			try {
				done(); // Signal other consumer threads 
			} catch (InterruptedException e) {
				Thread.currentThread().interrupt();
				return false;
			}
			return false;
		}

	}

	private final class Some implements Entry<T> {
		public final T item;

		public Some(T item) {
			this.item = item;
		}

		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			LOG.log(LOG_LEVEL, () -> "Processing element: " + item + " Thread: " + Thread.currentThread().getName());
			action.accept(item);
			return true;
		}

	}

	private final class Drainer implements Spliterator<T> {
		@Override
		public boolean tryAdvance(Consumer<? super T> action) {
			if (isClosed()) {
				return false;
			}
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
		public Spliterator<T> trySplit() {
			return null;
		}

		@Override
		public long estimateSize() {
			return Long.MAX_VALUE;
		}

		@Override
		public int characteristics() {
			return CONCURRENT;
		}

	}

	private void ensureOpen() throws ClosedException {
		if (isClosed()) {
			throw new ClosedException();
		}
	}

	private static int getParallelism() {
		return getPool().getParallelism();
	}

	private static ForkJoinPool getPool() {
		Thread t = Thread.currentThread();
		if (t instanceof ForkJoinWorkerThread fjwt) {
			return fjwt.getPool();
		}
		return ForkJoinPool.commonPool();
	}

	private final PreSplitSpliterator<T> presplit;
	private final AtomicBoolean closed = new AtomicBoolean(false);
	// Raw Object references and "instanceof" are slower than Entry wrapping
	private final ArrayBlockingQueue<Entry<T>> pipe;
	private final End end = new End();

}
