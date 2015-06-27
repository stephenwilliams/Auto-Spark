package com.alta189.test.auto.spark.server;

import com.alta189.auto.spark.AutoSparkUtils;
import com.alta189.auto.spark.SparkResponseTransformer;

public class Result {
	private final String result;

	public Result(String result) {
		this.result = result;
	}

	public String getResult() {
		return "{ \"result\" : \"" + result + "\" }";
	}

	public static class ResultTransformer extends SparkResponseTransformer {
		@Override
		public String render(Object model) throws Exception {
			Result result = AutoSparkUtils.safeCast(model);
			if (result == null) {
				throw new IllegalAccessException("result null");
			}
			return result.getResult();
		}
	}
}
