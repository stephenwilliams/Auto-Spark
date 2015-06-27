package com.alta189.auto.spark;

import spark.Request;
import spark.Response;

/**
 * Classes extending this will add a filter to Spark. If not marked with a {@link FilterMapping}.
 * the filter will be applied to all routes
 */
public abstract class SparkFilter {
	public SparkFilter() {
	}

	public abstract void before(Request request, Response response);

	public abstract void after(Request request, Response response);
}
