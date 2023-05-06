package org.basilevs.jstackfilter.test;

import java.util.List;
import java.util.Objects;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.DistinctBy;
import org.junit.Assert;
import org.junit.Test;

public class DistinctByTest {

	@Test
	public void combineSame() {
		var result = Stream.generate(() -> Integer.valueOf(1))
				.parallel()
				.limit(10000).collect(new DistinctBy<>(Objects::equals));
		Assert.assertEquals("No multiple entries of same value", List.of(1), result);
	}

	@Test
	public void combineDistinct() {
		var result = IntStream.range(0, 10000).mapToObj(Integer::valueOf)
				.parallel()
				.limit(10000).collect(new DistinctBy<>(Objects::equals));
		Assert.assertEquals(10000, result.size());
	}

}
