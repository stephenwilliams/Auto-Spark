package com.alta189.auto.spark;

import org.eclipse.jetty.util.StringUtil;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.ResponseTransformerRouteImpl;
import spark.RouteImpl;
import spark.TemplateViewRouteImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.List;

class ResourceRegistration extends Registration {
	private static final String EMPTY_STRING = "";
	private ResourceMapping resourceMapping;
	private Transformer transformerMapping;
	private TemplateEngine templateEngineMapping;
	private SparkResponseTransformer transformer = null;
	private spark.TemplateEngine templateEngine = null;
	private List<String> paths = new ArrayList<>();
	private RegistrationType registrationType;
	private boolean isVoid;

	public ResourceRegistration(AutoController parent, Method method) {
		super(parent, method);
	}

	public void build() throws RegistrationException {
		resourceMapping = getMethod().getAnnotation(ResourceMapping.class);
		if (resourceMapping == null) {
			throw new RegistrationException(getMethod(), "mapping annotation was null; Should have never happened");
		}

		if (!Modifier.isPublic(getMethod().getModifiers())) {
			throw new RegistrationException(getMethod(), "method has to be public");
		}

		if (Modifier.isStatic(getMethod().getModifiers())) {
			throw new RegistrationException(getMethod(), "method has to be public");
		}

		if (getMethod().getReturnType().equals(Void.TYPE)) {
			isVoid = true;
		}

		Class<?>[] parameters = getMethod().getParameterTypes();
		if (parameters.length != 2 || !parameters[0].equals(Request.class) || !parameters[1].equals(Response.class)) {
			throw new RegistrationException(getMethod(), "does not have the required parameters");
		}

		transformerMapping = getMethod().getAnnotation(Transformer.class);
		templateEngineMapping = getMethod().getAnnotation(TemplateEngine.class);
		if (transformerMapping != null && templateEngineMapping != null && !transformerMapping.value().equals(DefaultSparkResponseTransformer.class) && !templateEngineMapping.value().equals(DefaultTemplateEngine.class)) {
			throw new RegistrationException(getMethod(), "Transformer and Template Engine cannot both be set");
		}

		if (getTransformerMapping() != null && !getTransformerMapping().value().equals(DefaultSparkResponseTransformer.class)) {
			transformer = AutoSparkUtils.getObjectInstance(getTransformerMapping().value());
			if (getTransformer() == null) {
				throw new RegistrationException(getMethod(), "unable to create instance of the transformer");
			}
		}

		if (getTemplateEngineMapping() != null && !getTemplateEngineMapping().value().equals(DefaultTemplateEngine.class)) {
			templateEngine = AutoSparkUtils.getObjectInstance(getTemplateEngineMapping().value());
			if (getTemplateEngine() == null) {
				throw new RegistrationException(getMethod(), "unable to create instance of the template engine");
			}
			if (!getMethod().getReturnType().equals(ModelAndView.class)) {
				throw new RegistrationException(getMethod(), "when using a template engine you have to return spark.ModelAndView");
			}
		}

		if (getTransformer() == null && getTemplateEngine() == null) {
			if (!AutoSparkUtils.isIgnoreParent(getTransformerMapping()) && getParent().getTransformer() != null) {
				transformer = getParent().getTransformer();
			} else if (!AutoSparkUtils.isIgnoreParent(getTemplateEngineMapping()) && getParent().getTemplateEngine() != null && getMethod().getReturnType().equals(ModelAndView.class)) {
				templateEngine = getParent().getTemplateEngine();
			}
		}

		if (getParent().getResourceMapping() != null && getParent().getResourceMapping().value().length == 1 && StringUtil.isNotBlank(getParent().getResourceMapping().value()[0])) {
			for (String path : getResourceMapping().value()) {
				paths.add(getParent().getResourceMapping().value()[0] + path);
			}
		} else {
			for (String path : getResourceMapping().value()) {
				paths.add(path);
			}
		}

		if (transformer != null && isVoid) {
			throw new RegistrationException(getMethod(), "method return type cannot be void");
		} else if (templateEngine != null && isVoid) {
			throw new RegistrationException(getMethod(), "method return type cannot be void");
		}
	}

	@Override
	public void register() throws RegistrationException {
		if (transformer != null) {
			for (String path : getPaths()) {
				transformerRegister(path);
			}
			registrationType = RegistrationType.TRANSFORMER;
		} else if (templateEngine != null) {
			for (String path : getPaths()) {
				templateEngineRegister(path);
			}
			registrationType = RegistrationType.TEMPLATE_ENGINE;
		} else {
			for (String path : getPaths()) {
				plainRegister(path);
			}
			registrationType = RegistrationType.PLAIN;
		}
	}

	private void plainRegister(String path) throws RegistrationException {
		try {
			getParent().getAutoSpark().getAddRoute().invoke(null, getResourceMapping().method().name().toLowerCase(), new RouteImpl(path, getResourceMapping().accepts()) {
				@Override
				public Object handle(Request request, Response response) throws Exception {
					try {
						if (isVoid) {
							getMethod().invoke(getParent().getControllerInstance(), request, response);
							return EMPTY_STRING;
						} else {
							return getMethod().invoke(getParent().getControllerInstance(), request, response);
						}
					} catch (IllegalAccessException | InvocationTargetException e) {
						if (e.getCause() != null) {
							Exception ex = AutoSparkUtils.safeCast(e.getCause(), Exception.class);
							if (ex == null) {
								ex = new WrappedThrowable(e.getCause());
							}
							throw ex;
						} else {
							throw e;
						}
					}
				}
			});
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RegistrationException(getMethod(), "Exception when registering route", e);
		}
		registrationType = RegistrationType.PLAIN;
	}

	private void transformerRegister(String path) throws RegistrationException {
		try {
			getParent().getAutoSpark().getAddRoute().invoke(null, getResourceMapping().method().name().toLowerCase(), ResponseTransformerRouteImpl.create(
					path,
					getResourceMapping().accepts(),
					new RouteImpl(path, getResourceMapping().accepts()) {
						@Override
						public Object handle(Request request, Response response) throws Exception {
							try {
								return getMethod().invoke(getParent().getControllerInstance(), request, response);
							} catch (IllegalAccessException | InvocationTargetException e) {
								if (e.getCause() != null) {
									Exception ex = AutoSparkUtils.safeCast(e.getCause(), Exception.class);
									if (ex == null) {
										ex = new WrappedThrowable(e.getCause());
									}
									throw ex;
								} else {
									throw e;
								}
							}
						}
					},
					getTransformer()
			));
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		registrationType = RegistrationType.TRANSFORMER;
	}

	private void templateEngineRegister(String path) throws RegistrationException {
		try {
			getParent().getAutoSpark().getAddRoute().invoke(null, getResourceMapping().method().name().toLowerCase(), TemplateViewRouteImpl.create(
					path,
					getResourceMapping().accepts(),
					(request, response) -> {
						try {
							return (ModelAndView) getMethod().invoke(getParent().getControllerInstance(), request, response);
						} catch (IllegalAccessException | InvocationTargetException e) {
							if (e.getCause() != null) {
								throw new WrappedThrowableRuntimeException(e.getCause());
							} else {
								throw new WrappedThrowableRuntimeException(e);
							}
						}
					},
					getTemplateEngine()
			));
		} catch (IllegalAccessException | InvocationTargetException e) {
			e.printStackTrace();
		}
		registrationType = RegistrationType.TEMPLATE_ENGINE;
	}

	@Override
	public void print() {
		getPaths().forEach(this::print);
	}

	public void print(String path) {
		StringBuilder builder = new StringBuilder("[RESOURCE MAPPING] ")
				.append(AutoSparkUtils.getFullMethodName(getMethod()))
				.append(" ")
				.append(getResourceMapping().method().name())
				.append(" ")
				.append(path)
				.append(" accepts: ")
				.append(getResourceMapping().accepts())
				.append(" ");

		switch (registrationType) {
			case TRANSFORMER:
				builder.append(" transformer: ")
						.append(AutoSparkUtils.getSimpleClassName(transformer.getClass()));
				break;
			case TEMPLATE_ENGINE:
				builder.append(" template engine: ")
						.append(AutoSparkUtils.getSimpleClassName(templateEngine.getClass()));
				break;
			case PLAIN:
			default:
		}
		AutoSpark.getLogger().info(builder.toString());
	}

	public ResourceMapping getResourceMapping() {
		return resourceMapping;
	}

	public Transformer getTransformerMapping() {
		return transformerMapping;
	}

	public TemplateEngine getTemplateEngineMapping() {
		return templateEngineMapping;
	}

	public SparkResponseTransformer getTransformer() {
		return transformer;
	}

	public spark.TemplateEngine getTemplateEngine() {
		return templateEngine;
	}

	public List<String> getPaths() {
		return paths;
	}

	private enum RegistrationType {
		PLAIN,
		TRANSFORMER,
		TEMPLATE_ENGINE
	}
}
