package com.alta189.test.auto.spark.server;

import com.alta189.auto.spark.Controller;
import com.alta189.auto.spark.ResourceMapping;
import com.alta189.test.auto.spark.AutoSparkTestingConstants;
import org.junit.Assert;
import spark.Request;
import spark.Response;

@Controller
@ResourceMapping("/parent")
public class TestControllerTwo {
	@ResourceMapping("/child")
	public String child(Request request, Response response) {
		Assert.assertNotNull(request);
		Assert.assertNotNull(response);
		return AutoSparkTestingConstants.SUCCESS;
	}
}
