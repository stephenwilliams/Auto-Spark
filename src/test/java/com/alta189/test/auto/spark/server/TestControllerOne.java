package com.alta189.test.auto.spark.server;

import com.alta189.auto.spark.Controller;
import com.alta189.auto.spark.ResourceMapping;
import com.alta189.test.auto.spark.AutoSparkTestingConstants;
import org.junit.Assert;
import spark.Request;
import spark.Response;

@Controller
public class TestControllerOne {
	@ResourceMapping("/test")
	public String test(Request request, Response response) {
		Assert.assertNotNull(request);
		Assert.assertNotNull(response);
		return AutoSparkTestingConstants.SUCCESS;
	}

	@ResourceMapping(value = "/transformer", transformer = Result.ResultTransformer.class)
	public Result transformer(Request request, Response response) {
		Assert.assertNotNull(request);
		Assert.assertNotNull(response);
		return new Result(AutoSparkTestingConstants.SUCCESS);
	}
}
