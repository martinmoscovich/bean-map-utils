package com.mmoscovich.beanmap.utils;

import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

public class CollectionUtils {

	
	public static <R, T> Set<R> map(Set<T> collection, Function<? super T, ? extends R> mapper) {
		return collection.stream().map(mapper).collect(Collectors.toSet());
	}
	
	public static <R, T> List<R> map(List<T> collection, Function<? super T, ? extends R> mapper) {
		return collection.stream().map(mapper).collect(Collectors.toList());
	}
}
