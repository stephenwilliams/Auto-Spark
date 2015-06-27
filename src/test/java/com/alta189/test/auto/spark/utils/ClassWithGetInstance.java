package com.alta189.test.auto.spark.utils;

public class ClassWithGetInstance {
	private static final ClassWithGetInstance instance = new ClassWithGetInstance(null);

	private ClassWithGetInstance(Object object) {

	}

	public static ClassWithGetInstance getInstance() {
		return instance;
	}
}
