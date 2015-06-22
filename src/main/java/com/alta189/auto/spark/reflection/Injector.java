package com.alta189.auto.spark.reflection;

public interface Injector {
	public Object newInstance(Class<?> clazz);
}