package org.basilevs.jstackfilter.test;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import java.lang.System.Logger.Level;
import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.Spliterators;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import java.util.stream.StreamSupport;

import org.basilevs.jstackfilter.PushSpliterator;
import org.junit.Assert;
import org.junit.Test;

public class ParallelStreamTest {
	private static final Level LOG_LEVEL = System.Logger.Level.INFO;
	private static final System.Logger LOG = System.getLogger(ParallelStreamTest.class.getName());

	private final Set<String> producers = Collections.synchronizedSet(new HashSet<>());
	private final Set<String> processors = Collections.synchronizedSet(new HashSet<>());
	private final AtomicInteger concurrentProducers = new AtomicInteger();
	private Instant start = Instant.now();
	private static final Duration WORK_UNIT = Duration.ofMillis(10);
	private static final float PROCESSOR_WORK_RATIO = 3;  
	
	@Test
	public void badParallelExecution() {
		int count = 10; 
		createSerialStream(count)
			.map(this::process)
			.parallel()
			.forEach(i -> {});
		assertQuickerThan((PROCESSOR_WORK_RATIO + 1)*count);
	}

	@Test
	public void ensureRealParallelExecution() {
		int count = 15;
		defaultParallel(createSerialStream(count))
			.map(this::process)
			.parallel()
			.forEach(i -> {});
		assertQuickerThan(count + PROCESSOR_WORK_RATIO);
	}

	
	@Test
	public void close() {
		Stream<Object> input = Stream.empty();
		AtomicBoolean closed = new AtomicBoolean(false);
		input.onClose(() -> {
			closed.set(true);
		});
		defaultParallel(input).close();
		assertTrue(closed.get());
	}
	
	@Test
	public void closedInputShouldNotBeConsumed() {
		AtomicInteger consumed = new AtomicInteger(0);
		AtomicInteger closeCount = new AtomicInteger(0);
		Stream<Object> input = Stream.generate(this::produce).peek(ignored -> consumed.incrementAndGet()).onClose(() -> consumed.set(0)).onClose(() -> closeCount.incrementAndGet());
		try (Stream<Object> subject = defaultParallel(input).map(this::process)) {
			subject.forEach(ignored -> {throw new CancellationException();});
			Assert.fail("Should throw");
		} catch (CancellationException e ) {
			// expected
		}
		assertEquals(consumed.get(), 0);
		assertEquals(closeCount.get(), 1);
	}
	
	private <T> Stream<T> defaultParallel(Stream<T> serialStream) {
		return PushSpliterator.parallel(serialStream);
	}

	private final AtomicInteger count = new AtomicInteger(0);
	Object produce() {
		concurrentProducers.incrementAndGet();
		try {
			String name = Thread.currentThread().getName();
			Integer result = Integer.valueOf(count.getAndIncrement());
			LOG.log(LOG_LEVEL, "{0}, {1}", name, result);
			Thread.sleep(WORK_UNIT.toMillis());
			producers.add(name);
			return result;
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		} finally {
			Assert.assertEquals(0, concurrentProducers.decrementAndGet()); 
		}
	}

	private Object process(Object object1) {
		try {
			String name = Thread.currentThread().getName();
			LOG.log(LOG_LEVEL, "{0}, {1}", name, object1);
			Thread.sleep((long)(PROCESSOR_WORK_RATIO * WORK_UNIT.toMillis()));
		} catch (InterruptedException e) {
			throw new AssertionError(e);
		}
		processors.add(Thread.currentThread().getName());
		return new Object();
	}
	
	private Stream<Object> createSerialStream(int elementCount) {
		Iterator<Object> iterator = new Iterator<>() {

			private int i = 0;

			@Override
			public boolean hasNext() {
				return i < elementCount;
			}

			@Override
			public Object next() {
				i++;
				return produce();
			}
			
		};
		
		Stream<Object> serial = StreamSupport.stream(Spliterators.spliteratorUnknownSize(iterator, 0), false);
		return serial;
	}
	
	private void assertQuickerThan(float i) {
		Instant stop = Instant.now();
		Duration elapsed = Duration.between(start, stop);
		Duration expected = Duration.ofMillis((long) ( (WORK_UNIT.toMillis() + 10) * i));
		assertTrue(elapsed.toString() + " < " + expected, elapsed.compareTo(expected.plusMillis(150)) < 0);
	}

}
