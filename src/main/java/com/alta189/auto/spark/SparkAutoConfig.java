package com.alta189.auto.spark;

import com.alta189.auto.spark.reflection.EmptyFileUrlTypes;
import com.alta189.auto.spark.reflection.EmptyInjector;
import org.reflections.Reflections;
import org.reflections.util.ClasspathHelper;
import org.reflections.util.ConfigurationBuilder;
import org.reflections.util.FilterBuilder;
import org.reflections.vfs.Vfs;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import spark.Request;
import spark.Response;
import spark.Spark;
import spark.utils.StringUtils;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.reflections.ReflectionUtils.*;

/**
 * This class uses reflection to find and register methods and classes with
 * Spark
 */
public class SparkAutoConfig {
	private static final Logger logger = LoggerFactory.getLogger(SparkAutoConfig.class);
	private final List<String> excludedPackages = new ArrayList<>();
	private final List<String> excludedPatterns = new ArrayList<>();
	private final List<String> excludedFileExtensions = new ArrayList<>();

	public SparkAutoConfig() {
		excludedPackages.add("spark");
		excludedPackages.add("java");
		excludedPackages.add("javax");
		excludedPackages.add("org.sun");
		excludedPackages.add("org.ietf.jgss");
		excludedPackages.add("org.w3c.dom");
		excludedPackages.add("org.xml.sax");
		excludedPackages.add("org.omg");
		excludedPackages.add("com.alta189.auto.spark");
		excludedFileExtensions.add(".jnilib");
	}

	/**
	 * Runs the scan on the calling classes package
	 */
	public void run() {
		try {
			String className = new Exception().getStackTrace()[1].getClassName();
			Class<?> clazz = Class.forName(className);
			run(clazz.getPackage().getName());
		} catch (ClassNotFoundException e) {
			throw new RuntimeException("Error getting calling class", e);
		}
	}

	/**
	 * Runs the scan on all classes in the ClassLoader
	 */
	public void runFull() {
		init();
		Reflections reflections = new Reflections(getConfigurationBuilder().addUrls(ClasspathHelper.forClassLoader()));
		run(reflections);

	}

	/**
	 * Runs the scan only on the specified package
	 *
	 * @param searchPackage package to search
	 */
	public void run(String searchPackage) {
		System.out.println("SparkAutoConfig.run");
		init();
		Reflections reflections = new Reflections(getConfigurationBuilder().addUrls(ClasspathHelper.forPackage(searchPackage)));
		run(reflections);
	}

	/**
	 * Runs with a reflections object
	 *
	 * @param reflections see {@link Reflections}
	 */
	public void run(Reflections reflections) {
		registerControllers(reflections);
		registerFilters(reflections);
	}

	/**
	 * Excludes a package from the search
	 *
	 * @param excludedPackage package to exlude
	 */
	public void exludePackage(String excludedPackage) {
		excludedPackages.add(excludedPackage);
	}

	/**
	 * Excludes based on a regex pattern
	 *
	 * @param excludedPattern pattern to exlude
	 */
	public void exludePattern(String excludedPattern) {
		excludedPatterns.add(excludedPattern);
	}

	/**
	 * Excludes a file extension from the {@link Reflections} search
	 *
	 * @param fileExtensions extension to exclude
	 */
	public void exludeFileExtensions(String fileExtensions) {
		excludedFileExtensions.add(fileExtensions);
	}

	private void init() {
		final List<Vfs.UrlType> urlTypes = new ArrayList<>();
		urlTypes.add(new EmptyFileUrlTypes(excludedFileExtensions));
		urlTypes.addAll(Arrays.asList(Vfs.DefaultUrlTypes.values()));
		Vfs.setDefaultURLTypes(urlTypes);
	}

	private ConfigurationBuilder getConfigurationBuilder() {
		ConfigurationBuilder configurationBuilder = new ConfigurationBuilder();
		for (String excludedPackage : excludedPackages) {
			configurationBuilder.filterInputsBy(new FilterBuilder().excludePackage(excludedPackage));
		}
		for (String excludedPattern : excludedPatterns) {
			configurationBuilder.filterInputsBy(new FilterBuilder().exclude(excludedPattern));
		}
		return configurationBuilder;
	}

	private void registerControllers(Reflections reflections) {
		Set<Class<?>> controllers = reflections.getTypesAnnotatedWith(Controller.class);

		controllers.forEach(controller -> {
			registerController(controller);
			registerExceptionHandler(controller);
		});

	}

	private void registerController(Class<?> controller) {
		ResourceMapping parentResourceMapping = controller.getAnnotation(ResourceMapping.class);
		final Object instance = getInstance(controller);

		Set<Method> methods = getAllMethods(controller, withAnnotation(ResourceMapping.class), withModifier(Modifier.PUBLIC));
		if (methods == null || methods.size() == 0) {
			return;
		}
		for (final Method method : methods) {
			method.setAccessible(true);

			ResourceMapping mapping = method.getAnnotation(ResourceMapping.class);
			if (mapping == null || StringUtils.isEmpty(mapping.value())) {
				continue;
			}

			Class<?>[] parameters = method.getParameterTypes();
			if (parameters.length != 2 || !parameters[0].equals(Request.class) || !parameters[1].equals(Response.class)) {
				logger.error(controller.getCanonicalName() + ":" + method.getName() + " does not have the required parameters");
				continue;
			}

			if (method.getReturnType().equals(Void.TYPE)) {
				logger.error(controller.getCanonicalName() + ":" + method.getName() + " needs to return an object");
			}

			String path = mapping.value();
			if (parentResourceMapping != null && StringUtils.isNotEmpty(parentResourceMapping.value())) {
				path = parentResourceMapping.value() + path;
			}

			if (mapping.transformer().equals(DefaultSparkResponseTransformer.class)) {
				switch (mapping.method()) {
					case GET:
						Spark.get(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response));
						break;
					case POST:
						Spark.post(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response));
						break;
					case PUT:
						Spark.put(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response));
						break;
					case DELETE:
						Spark.delete(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response));
						break;
					case HEAD:
						Spark.head(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response));
						break;
					case TRACE:
						Spark.trace(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response));
						break;
					case CONNECT:
						Spark.connect(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response));
						break;
					case OPTIONS:
						Spark.options(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response));
						break;
				}
			} else {
				final SparkResponseTransformer transformer = getInstance(mapping.transformer());
				if (instance == null) {
					logger.error(controller.getCanonicalName() + ":" + method.getName() + " Error creating instance of the transformer");
				}

				switch (mapping.method()) {
					case GET:
						Spark.get(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response, transformer));
						break;
					case POST:
						Spark.post(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response, transformer));
						break;
					case PUT:
						Spark.put(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response, transformer));
						break;
					case DELETE:
						Spark.delete(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response, transformer));
						break;
					case HEAD:
						Spark.head(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response, transformer));
						break;
					case TRACE:
						Spark.trace(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response, transformer));
						break;
					case CONNECT:
						Spark.connect(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response, transformer));
						break;
					case OPTIONS:
						Spark.options(path, mapping.accepts(), (request, response) -> method.invoke(instance, request, response, transformer));
						break;
				}
			}
		}
	}

	private void registerExceptionHandler(Class<?> controller) {
		final Object instance = getInstance(controller);

		Set<Method> methods = getAllMethods(controller, withAnnotation(ExceptionMapping.class), withModifier(Modifier.PUBLIC), withParametersCount(3), withParametersAssignableTo(Throwable.class, Request.class, Response.class));
		System.out.println("methods = " + methods);
		if (methods == null || methods.size() == 0) {
			return;
		}

		for (final Method method : methods) {
			method.setAccessible(true);

			ExceptionMapping mapping = method.getAnnotation(ExceptionMapping.class);
			if (mapping == null) {
				continue;
			}

			Spark.exception(mapping.value(), (e, request, response) -> {
				try {
					method.invoke(instance, e, request, response);
				} catch (IllegalAccessException | InvocationTargetException e1) {
					e1.printStackTrace();
				}
			});
		}
	}

	private void registerFilters(Reflections reflections) {
		Set<Class<? extends SparkFilter>> filters = reflections.getSubTypesOf(SparkFilter.class);

		filters.forEach(this::registerFilter);
	}

	private void registerFilter(Class<? extends SparkFilter> filterClass) {
		FilterMapping filterMapping = filterClass.getAnnotation(FilterMapping.class);
		SparkFilter filter = getInstance(filterClass);

		if (filter == null){
			logger.error(filterClass.getName() + " had trouble getting an instance of the filter");
			return;
		}

		if (filterMapping.value() == null || StringUtils.isEmpty(filterMapping.value().trim())) {
			Spark.before(filter::before);
			Spark.after(filter::after);
		} else {
			Spark.before(filterMapping.value(), filter::before);
			Spark.after(filterMapping.value(), filter::after);
		}
	}

	private  <T> T getInstance(Class<? extends T> clazz) {
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
					instance = (T) methods.iterator().next().invoke(null);
				}
			} catch (Exception ignored) {
			}

			if (instance == null) {
				instance = (T) EmptyInjector.getInstance().newInstance(clazz);
			}
		} catch (Exception e) {
			e.printStackTrace();
			return null;
		}
		return instance;
	}
}
