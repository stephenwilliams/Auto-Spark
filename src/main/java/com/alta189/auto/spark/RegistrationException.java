package com.alta189.auto.spark;

import java.lang.reflect.Method;

public class RegistrationException extends Exception {
	public RegistrationException(Class<?> clazz, String s) {
		super(generateMessage(clazz, s));
	}

	public RegistrationException(Class<?> clazz, String s, Throwable throwable) {
		super(generateMessage(clazz, s), throwable);
	}

	public RegistrationException(Class<?> clazz, Throwable throwable) {
		super(generateMessage(clazz), throwable);
	}

	public RegistrationException(Method method, String s) {
		super(generateMessage(method, s));
	}

	public RegistrationException(Method method, String s, Throwable throwable) {
		super(generateMessage(method, s), throwable);
	}

	public RegistrationException(Method method, Throwable throwable) {
		super(generateMessage(method), throwable);
	}

	private static String generateMessage(Class<?> clazz) {
		return AutoSparkUtils.getSimpleClassName(clazz);
	}

	private static String generateMessage(Class<?> clazz, String message) {
		return AutoSparkUtils.getSimpleClassName(clazz) + ": " + message;
	}

	private static String generateMessage(Method method) {
		return AutoSparkUtils.getFullMethodName(method);
	}

	private static String generateMessage(Method method, String message) {
		return AutoSparkUtils.getFullMethodName(method) + ": " + message;
	}
}
