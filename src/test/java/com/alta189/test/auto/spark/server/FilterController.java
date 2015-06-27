package com.alta189.test.auto.spark.server;

import com.alta189.auto.spark.Controller;
import com.alta189.auto.spark.ResourceMapping;
import com.alta189.test.auto.spark.AutoSparkTestingConstants;
import org.junit.Assert;
import spark.Request;
import spark.Response;

@Controller
public class FilterController {
	@ResourceMapping("/filter")
	public String filterTest(Request request, Response response) {
		Assert.assertNotNull(request);
		Assert.assertNotNull(response);
		return AutoSparkTestingConstants.SUCCESS;
	}
}
