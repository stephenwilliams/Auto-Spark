package com.alta189.auto.spark;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Can be placed on a Method or Class. Class containing this must have
 * the {@link Controller} annotation and also have methods with the {@link ResourceMapping}
 * annotation
 * <p>
 * At the class level, it defines the default Template Engine for all routes
 * in that class
 */
@Target({ElementType.TYPE, ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface TemplateEngine {
	/**
	 * The template engine for the route
	 * <p>
	 * Cannot be used in conjunction with a transformer
	 *
	 * @return template engine class
	 */
	Class<? extends spark.TemplateEngine> value() default DefaultTemplateEngine.class;

	/**
	 * Denotes if the parent class's template engine should be ignored. If true,
	 * the parent's template engine will not be used for this mapping
	 *
	 * @return flag to ignore parent template engine
	 */
	boolean ignoreParent() default false;
}
