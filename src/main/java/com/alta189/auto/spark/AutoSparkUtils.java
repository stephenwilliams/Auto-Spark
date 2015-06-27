package com.alta189.auto.spark;

import com.alta189.auto.spark.reflection.EmptyInjector;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.Set;

import static org.reflections.ReflectionUtils.getAllFields;
import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.ReflectionUtils.withModifier;
import static org.reflections.ReflectionUtils.withName;
import static org.reflections.ReflectionUtils.withParametersCount;
import static org.reflections.ReflectionUtils.withReturnType;
import static org.reflections.ReflectionUtils.withType;

@SuppressWarnings("unchecked")
public class AutoSparkUtils {
	public static <T> T getObjectInstance(Class<? extends T> clazz) {
		T instance = null;
		try {
			try {
				Set<Method> methods = getAllMethods(clazz,
						withModifier(Modifier.PUBLIC),
						withModifier(Modifier.STATIC),
						withReturnType(clazz),
						withName("getInstance"),
						withParametersCount(0));
				if (methods != null && methods.size() == 1) {
					instance = safeCast(methods.iterator().next().invoke(null));
				}
			} catch (Exception ignored) {
			}

			if (instance != null) {
				return instance;
			}

			try {
				Set<Field> fields = getAllFields(clazz,
						withModifier(Modifier.PUBLIC),
						withModifier(Modifier.STATIC),
						withType(clazz),
						withName("INSTANCE"));
				if (fields != null && fields.size() == 1) {
					instance = safeCast(fields.iterator().next().get(null));
				}
			} catch (Exception ignored) {
			}

			if (instance == null) {
				instance = safeCast(EmptyInjector.getInstance().newInstance(clazz));
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return instance;
	}

	public static <T> T safeCast(Object object) {
		try {
			return (T) object;
		} catch (ClassCastException ignored) {
		}
		return null;
	}

	public static <T> T safeCast(Object object, Class<T> type) {
		try {
			return (T) object;
		} catch (ClassCastException ignored) {
		}
		return null;
	}
}
