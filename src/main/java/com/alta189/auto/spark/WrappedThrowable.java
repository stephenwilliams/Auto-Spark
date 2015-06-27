package com.alta189.auto.spark;

public class WrappedThrowable extends Exception {
	public WrappedThrowable(Throwable cause) {
		super("Caught throwable that was not an exception", cause);
	}
}
