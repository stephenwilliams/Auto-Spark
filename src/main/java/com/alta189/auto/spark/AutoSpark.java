package com.alta189.auto.spark;

import com.alta189.auto.spark.reflection.EmptyFileUrlTypes;
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
import spark.exception.ExceptionHandlerImpl;
import spark.exception.ExceptionMapper;
import spark.utils.StringUtils;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.ReflectionUtils.withModifier;
import static org.reflections.ReflectionUtils.withName;

/**
 * This class uses reflection to find and register methods and classes with
 * Spark
 */
public class AutoSpark {
	private static final Logger logger = LoggerFactory.getLogger(AutoSpark.class);
	private final List<String> excludedPackages = new ArrayList<>();
	private final List<String> excludedPatterns = new ArrayList<>();
	private final List<String> excludedFileExtensions = new ArrayList<>();
	private final List<AutoController> registeredControllers = new ArrayList<>();
	private final List<String> registeredFilters = new ArrayList<>();
	protected Method addRoute;

	public AutoSpark() {
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
		addRoute = getAllMethods(Spark.class, withModifier(Modifier.PROTECTED), withName("addRoute")).iterator().next();
		addRoute.setAccessible(true);
		registerControllers(reflections);
		registerFilters(reflections);
		registerWrappedRuntimeExceptionCatch();
		Spark.awaitInitialization();
		printRegistrations();
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

		controllers.forEach(this::registerController);
	}

	private void registerController(Class<?> controllerClass) {
		AutoController controller;
		try {
			controller = new AutoController(this, controllerClass);
			controller.build();
			controller.register();
			registeredControllers.add(controller);
		} catch (RegistrationException e) {
			logger.error("exception when building controller", e);
		}
	}

	private void registerFilters(Reflections reflections) {
		Set<Class<? extends SparkFilter>> filters = reflections.getSubTypesOf(SparkFilter.class);

		filters.forEach(this::registerFilter);
	}

	private void registerFilter(Class<? extends SparkFilter> filterClass) {
		FilterMapping filterMapping = filterClass.getAnnotation(FilterMapping.class);
		SparkFilter filter = AutoSparkUtils.getObjectInstance(filterClass);

		if (filter == null) {
			logger.error(filterClass.getName() + " had trouble getting an instance of the filter");
			return;
		}
		if (filterMapping == null || filterMapping.value() == null || StringUtils.isEmpty(filterMapping.value().length == 0)) {
			registerFilter(filter);
		} else {
			for (String path : filterMapping.value()) {
				registerFilter(path, filter);
			}
		}
	}

	private void registerFilter(SparkFilter filter) {
		Spark.before(filter::before);
		Spark.after(filter::after);
		registeredFilters.add("[FILTER MAPPING] " + AutoSparkUtils.getSimpleClassName(filter.getClass()));
	}

	private void registerFilter(String path, SparkFilter filter) {
		StringBuilder builder = new StringBuilder("[FILTER MAPPING] ").append(AutoSparkUtils.getSimpleClassName(filter.getClass()));
			Spark.before(path, filter::before);
			Spark.after(path, filter::after);
			builder.append("path: ").append(path);
		registeredFilters.add(builder.toString());
	}

	private void registerWrappedRuntimeExceptionCatch() {
		Spark.exception(WrappedThrowableRuntimeException.class, (wrappedThrowable, request, response) -> handleWrappedRuntimeException(AutoSparkUtils.safeCast(wrappedThrowable, WrappedThrowableRuntimeException.class), request, response));
	}

	private void handleWrappedRuntimeException(WrappedThrowableRuntimeException wrappedThrowable, Request request, Response response) {
		if (wrappedThrowable.getCause() == null) {
			return;
		}

		Exception exception = AutoSparkUtils.safeCast(wrappedThrowable.getCause());
		if (exception == null) {
			throw wrappedThrowable;
		}

		ExceptionHandlerImpl handler = ExceptionMapper.getInstance().getHandler(exception);
		if (handler == null) {
			throw wrappedThrowable;
		}
		handler.handle(exception, request, response);
	}

	public void printRegistrations() {
		registeredControllers.forEach(AutoController::print);
		registeredFilters.forEach(logger::info);
	}

	protected Method getAddRoute() {
		return addRoute;
	}

	public static Logger getLogger() {
		return logger;
	}
}
