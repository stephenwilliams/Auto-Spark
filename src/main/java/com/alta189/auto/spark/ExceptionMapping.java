package com.alta189.auto.spark;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Denotes a method to handle an {@link Exception}. Class containing this must have
 * the {@link Controller} annotation
 *
 * The method must return an object and have {@link spark.Request}
 * and {@link spark.Response} as its parameters.
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionMapping {
	Class<? extends Exception> value();
}
