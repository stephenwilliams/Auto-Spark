package com.alta189.auto.spark;

public class WrappedThrowableRuntimeException extends RuntimeException {
	public WrappedThrowableRuntimeException(Throwable cause) {
		super(cause);
	}
}
