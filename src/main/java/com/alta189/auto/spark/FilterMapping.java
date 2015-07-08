package com.alta189.auto.spark;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * The path to apply the {@link SparkFilter} to
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface FilterMapping {
	/**
	 * The path to apply the {@link SparkFilter} to
	 *
	 * @return path
	 */
	String[] value();
}
