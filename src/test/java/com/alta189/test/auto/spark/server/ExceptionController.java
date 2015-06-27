package com.alta189.test.auto.spark.server;

import com.alta189.auto.spark.Controller;
import com.alta189.auto.spark.ExceptionMapping;
import com.alta189.auto.spark.ResourceMapping;
import com.alta189.test.auto.spark.AutoSparkTestingConstants;
import org.junit.Assert;
import spark.Request;
import spark.Response;

@Controller
public class ExceptionController {
	@ResourceMapping("/throw")
	public String throwException(Request request, Response response) throws NotFoundException {
		Assert.assertNotNull(request);
		Assert.assertNotNull(response);
		throw new NotFoundException();
	}

	@ExceptionMapping(NotFoundException.class)
	public void notFound(NotFoundException nfe, Request request, Response response) {
		Assert.assertNotNull(nfe);
		Assert.assertNotNull(request);
		Assert.assertNotNull(response);
		response.body(AutoSparkTestingConstants.SUCCESS);
	}
}
