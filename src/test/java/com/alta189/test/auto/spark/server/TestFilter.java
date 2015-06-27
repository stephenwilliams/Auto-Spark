package com.alta189.test.auto.spark.server;

import com.alta189.auto.spark.FilterMapping;
import com.alta189.auto.spark.SparkFilter;
import com.alta189.test.auto.spark.AutoSparkTest;
import org.junit.Assert;
import spark.Request;
import spark.Response;

@FilterMapping("/filter")
public class TestFilter extends SparkFilter {
	@Override
	public void before(Request request, Response response) {
		Assert.assertNotNull(request);
		Assert.assertNotNull(response);
		AutoSparkTest.BEFORE_FILTER = true;
	}

	@Override
	public void after(Request request, Response response) {
		Assert.assertNotNull(request);
		Assert.assertNotNull(response);
		AutoSparkTest.AFTER_FILTER = true;
	}
}
