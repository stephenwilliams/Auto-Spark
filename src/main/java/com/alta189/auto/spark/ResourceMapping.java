package com.alta189.auto.spark;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be placed on a Method or Class. Class containing this must have
 * the {@link Controller} annotation
 * <p>
 * When placed on a method it denotes a route for Spark. The method must
 * return an object and have {@link spark.Request} and {@link spark.Response} as
 * its parameters
 * <p>
 * When placed on a class it's value is used as the prefix for the value of
 * all methods marked with {@link ResourceMapping}
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceMapping {
	/**
	 * Path for the Route
	 *
	 * @return path
	 */
	String[] value();

	/**
	 * Method for the Route.
	 * <p>
	 * Default is GET
	 *
	 * @return method
	 */
	RequestMethod method() default RequestMethod.GET;

	/**
	 * The accept type
	 *
	 * @return accept type
	 */
	String accepts() default "*/*";
}
