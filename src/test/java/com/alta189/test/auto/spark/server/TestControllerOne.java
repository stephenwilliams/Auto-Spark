package com.alta189.test.auto.spark.server;

import com.alta189.auto.spark.Controller;
import com.alta189.auto.spark.ResourceMapping;
import com.alta189.auto.spark.TemplateEngine;
import com.alta189.auto.spark.Transformer;
import com.alta189.test.auto.spark.AutoSparkTestingConstants;
import org.junit.Assert;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.template.freemarker.FreeMarkerEngine;

import java.util.HashMap;
import java.util.Map;

@Controller
public class TestControllerOne {
	@ResourceMapping("/test")
	public String test(Request request, Response response) {
		Assert.assertNotNull(request);
		Assert.assertNotNull(response);
		return AutoSparkTestingConstants.SUCCESS;
	}

	@ResourceMapping("/transformer")
	@Transformer(Result.ResultTransformer.class)
	public Result transformer(Request request, Response response) {
		Assert.assertNotNull(request);
		Assert.assertNotNull(response);
		return new Result(AutoSparkTestingConstants.SUCCESS);
	}

	@ResourceMapping("/template")
	@TemplateEngine(FreeMarkerEngine.class)
	public ModelAndView template(Request request, Response response) {
		Map<String, Object> attributes = new HashMap<>();
		attributes.put("message", AutoSparkTestingConstants.SUCCESS);
		return new ModelAndView(attributes, "hello.ftl");
	}

	@ResourceMapping("/template/throw")
	@TemplateEngine(FreeMarkerEngine.class)
	public ModelAndView templateError(Request request, Response response) throws NotFoundException {
		throw new NotFoundException();
	}
}
