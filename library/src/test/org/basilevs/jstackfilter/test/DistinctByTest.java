package org.basilevs.jstackfilter.test;

import java.util.List;
import java.util.Objects;
import java.util.stream.Stream;

import org.basilevs.jstackfilter.DistinctBy;
import org.junit.Assert;
import org.junit.Test;

public class DistinctByTest {

	@Test
	public void testCombine() {
		var result = Stream.generate(() -> Integer.valueOf(1))
				.parallel()
				.limit(10000).collect(new DistinctBy<>(Objects::equals));
		Assert.assertEquals("No multiple entries of same value", List.of(1), result);
	}

}
