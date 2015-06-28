package com.alta189.auto.spark;

import javassist.Modifier;
import spark.Request;
import spark.Response;
import spark.Spark;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

class ExceptionRegistration extends Registration {
	private ExceptionMapping exceptionMapping;

	public ExceptionRegistration(AutoController parent, Method method) {
		super(parent, method);
	}

	@Override
	public void build() throws RegistrationException {
		exceptionMapping = getMethod().getAnnotation(ExceptionMapping.class);
		if (exceptionMapping == null) {
			throw new RegistrationException(getMethod(), "mapping annotation was null; Should have never happened");
		}

		if (!Modifier.isPublic(getMethod().getModifiers())) {
			throw new RegistrationException(getMethod(), "method has to be public");
		}

		if (Modifier.isStatic(getMethod().getModifiers())) {
			throw new RegistrationException(getMethod(), "method has to be public");
		}

		if (!getMethod().getReturnType().equals(Void.TYPE)) {
			throw new RegistrationException(getMethod(), "method return type has to be void");
		}

		Class<?>[] parameters = getMethod().getParameterTypes();
		if (parameters.length != 3 || !Throwable.class.isAssignableFrom(parameters[0]) || !parameters[1].equals(Request.class) || !parameters[2].equals(Response.class)) {
			throw new RegistrationException(getMethod(), "does not have the required parameters");
		}
	}

	@Override
	public void register() throws RegistrationException {
		Spark.exception(getExceptionMapping().value(), (e, request, response) -> {
			try {
				getMethod().invoke(getParent().getControllerInstance(), e, request, response);
			} catch (IllegalAccessException | InvocationTargetException e1) {
				e1.printStackTrace();
			}
		});
	}

	public ExceptionMapping getExceptionMapping() {
		return exceptionMapping;
	}
}
