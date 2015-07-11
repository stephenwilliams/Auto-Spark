package com.alta189.auto.spark;

import java.lang.reflect.Method;

public abstract class Registration {
	private final AutoController parent;
	private final Method method;

	public Registration(AutoController parent, Method method) {
		this.parent = parent;
		this.method = method;
	}

	public abstract void build() throws RegistrationException;

	public abstract void register() throws RegistrationException;

	public abstract void print();

	public AutoController getParent() {
		return parent;
	}

	public Method getMethod() {
		return method;
	}
}
