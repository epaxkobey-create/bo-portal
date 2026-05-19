package com.nv.commons.manager;

@FunctionalInterface
public interface ThreadProcessor<T> {

	public T process() throws Exception;
}
