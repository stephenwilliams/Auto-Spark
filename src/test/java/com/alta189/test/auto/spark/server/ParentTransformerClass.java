package com.alta189.test.auto.spark.server;

import com.alta189.auto.spark.Controller;
import com.alta189.auto.spark.ResourceMapping;
import com.alta189.auto.spark.Transformer;
import com.alta189.test.auto.spark.AutoSparkTestingConstants;
import org.junit.Assert;
import spark.Request;
import spark.Response;

@Controller
@Transformer(Result.ResultTransformer.class)
public class ParentTransformerClass {
	@ResourceMapping("/parent/transformer")
	public Result childTransformer(Request request, Response response) {
		Assert.assertNotNull(request);
		Assert.assertNotNull(response);
		return new Result(AutoSparkTestingConstants.SUCCESS);
	}

	@ResourceMapping("/parent/transformer/ignore")
	@Transformer(ignoreParent = true)
	public String childTransformerIgnore(Request request, Response response) {
		Assert.assertNotNull(request);
		Assert.assertNotNull(response);
		return AutoSparkTestingConstants.SUCCESS;
	}
}
