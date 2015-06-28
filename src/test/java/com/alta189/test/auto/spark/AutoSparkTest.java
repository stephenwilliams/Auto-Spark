package com.alta189.test.auto.spark;

import com.alta189.auto.spark.AutoSpark;
import com.alta189.test.auto.spark.server.TestControllerOne;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import spark.Spark;

public class AutoSparkTest {
	public static boolean BEFORE_FILTER = false;
	public static boolean AFTER_FILTER = false;
	private final OkHttpClient client = new OkHttpClient();

	@BeforeClass
	public static void setup() {
		Spark.port(AutoSparkTestingConstants.SPARK_PORT);

		new AutoSpark().run(TestControllerOne.class.getPackage().getName());

		Spark.awaitInitialization();
	}

	@AfterClass
	public static void tearDown() {
		Spark.stop();
	}

	@Test
	public void testController() throws Exception {
		Response response = client.newCall(new Request.Builder().url(AutoSparkTestingConstants.HOST + "/test").build()).execute();
		Assert.assertNotNull(response);
		Assert.assertEquals(AutoSparkTestingConstants.SUCCESS, response.body().string());
	}

	@Test
	public void testTransformer() throws Exception {
		Response response = client.newCall(new Request.Builder().url(AutoSparkTestingConstants.HOST + "/transformer").build()).execute();
		Assert.assertNotNull(response);
		Assert.assertEquals(AutoSparkTestingConstants.EXPECTED_TRANSFORMER, response.body().string());
	}

	@Test
	public void testTemplateEngine() throws Exception {
		Response response = client.newCall(new Request.Builder().url(AutoSparkTestingConstants.HOST + "/template").build()).execute();
		Assert.assertNotNull(response);
		Assert.assertEquals(AutoSparkTestingConstants.SUCCESS, response.body().string());
	}

	@Test
	public void testTemplateEngineException() throws Exception {
		Response response = client.newCall(new Request.Builder().url(AutoSparkTestingConstants.HOST + "/template/throw").build()).execute();
		Assert.assertNotNull(response);
		Assert.assertEquals(AutoSparkTestingConstants.SUCCESS, response.body().string());
	}

	@Test
	public void testParentTransformer() throws Exception {
		Response response = client.newCall(new Request.Builder().url(AutoSparkTestingConstants.HOST + "/parent/transformer").build()).execute();
		Assert.assertNotNull(response);
		Assert.assertEquals(AutoSparkTestingConstants.EXPECTED_TRANSFORMER, response.body().string());
	}

	@Test
	public void testParentTransformerIgnored() throws Exception {
		Response response = client.newCall(new Request.Builder().url(AutoSparkTestingConstants.HOST + "/parent/transformer/ignore").build()).execute();
		Assert.assertNotNull(response);
		Assert.assertEquals(AutoSparkTestingConstants.SUCCESS, response.body().string());
	}

	@Test
	public void testFilter() throws Exception {
		Response response = client.newCall(new Request.Builder().url(AutoSparkTestingConstants.HOST + "/filter").build()).execute();
		Assert.assertNotNull(response);
		Assert.assertEquals(AutoSparkTestingConstants.SUCCESS, response.body().string());
		Assert.assertTrue(BEFORE_FILTER);
		Assert.assertTrue(AFTER_FILTER);
	}

	@Test
	public void testExceptionMapping() throws Exception {
		Response response = client.newCall(new Request.Builder().url(AutoSparkTestingConstants.HOST + "/throw").build()).execute();
		Assert.assertNotNull(response);
		Assert.assertEquals(AutoSparkTestingConstants.SUCCESS, response.body().string());
	}
}
