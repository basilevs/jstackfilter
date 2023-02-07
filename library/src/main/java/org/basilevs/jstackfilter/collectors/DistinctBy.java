package org.basilevs.jstackfilter.collectors;


import java.util.ArrayList;
import java.util.Collection;
import java.util.Objects;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.BiPredicate;
import java.util.function.BinaryOperator;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collector;

public class DistinctBy<T> implements Collector<T, Collection<T>, Collection<T>> {
	
	private final BiPredicate<T, T> predicate;
	
	public DistinctBy(BiPredicate<T, T> predicate) {
		super();
		this.predicate = Objects.requireNonNull(predicate);
	}

	@Override
	public Supplier<Collection<T>> supplier() {
		return ArrayList::new;
	}

	@Override
	public BiConsumer<Collection<T>, T> accumulator() {
		return (list, item) -> {
			for (var i : list) {
				if (predicate.test(item, i)) {
					return;
				}
			}
			list.add(item);
		};
	}

	@Override
	public BinaryOperator<Collection<T>> combiner() {
		return (list1, list2) -> {
			list1.addAll(list2);
			return list1;
		};
	}

	@Override
	public Function<Collection<T>, Collection<T>> finisher() {
		return Function.identity();
	}

	@Override
	public Set<Characteristics> characteristics() {
		return Set.of();
	}

}
