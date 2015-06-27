package com.alta189.test.auto.spark;

import com.alta189.auto.spark.AutoSparkUtils;
import com.alta189.test.auto.spark.utils.ClassWithGetInstance;
import com.alta189.test.auto.spark.utils.ClassWithInstance;
import com.alta189.test.auto.spark.utils.ImpossibleClass;
import com.alta189.test.auto.spark.utils.PlainClass;
import org.junit.Assert;
import org.junit.Test;

public class AutoSparkUtilsTest {
	@Test
	public void testGetInstance() {
		ClassWithGetInstance instance = AutoSparkUtils.getObjectInstance(ClassWithGetInstance.class);
		Assert.assertNotNull(instance);
	}

	@Test
	public void testInstance() {
		ClassWithInstance instance = AutoSparkUtils.getObjectInstance(ClassWithInstance.class);
		Assert.assertNotNull(instance);
	}

	@Test
	public void testCreateInstance() {
		PlainClass instance = AutoSparkUtils.getObjectInstance(PlainClass.class);
		Assert.assertNotNull(instance);
	}

	@Test
	public void testUnableToCreate() {
		ImpossibleClass instance = AutoSparkUtils.getObjectInstance(ImpossibleClass.class);
		Assert.assertNull(instance);
	}
}
