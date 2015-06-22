package com.alta189.auto.spark.reflection;

public class EmptyInjector implements Injector {
	private static final EmptyInjector instance = new EmptyInjector();

	public static EmptyInjector getInstance() {
		return instance;
	}

	public Object newInstance(Class<?> clazz) {
		try {
			return clazz.newInstance();
		} catch (InstantiationException e) {
			throw new InjectorException("Could not create a new instance of class '" + clazz.getCanonicalName() + "'", e.getCause());
		} catch (IllegalAccessException e) {
			throw new InjectorException("Could not create a new instance of class '" + clazz.getCanonicalName() + "'", e.getCause());
		}
	}
}