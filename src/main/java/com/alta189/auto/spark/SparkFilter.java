package com.alta189.auto.spark;

import spark.Request;
import spark.Response;

public abstract class SparkFilter {
	public SparkFilter() {
	}

	public abstract void before(Request request, Response response);

	public abstract void after(Request request, Response response);
}
