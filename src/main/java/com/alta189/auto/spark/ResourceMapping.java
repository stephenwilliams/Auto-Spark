package com.alta189.auto.spark;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ResourceMapping {
	String value();
	RequestMethod method() default RequestMethod.GET;
	String accepts() default "*/*";
	Class<? extends SparkResponseTransformer> transformer() default DefaultSparkResponseTransformer.class;
}
