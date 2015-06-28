package com.alta189.auto.spark;

import org.eclipse.jetty.util.StringUtil;
import spark.ModelAndView;
import spark.Request;
import spark.Response;
import spark.ResponseTransformerRouteImpl;
import spark.RouteImpl;
import spark.TemplateViewRoute;
import spark.TemplateViewRouteImpl;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;

class ResourceRegistration extends Registration {
	private ResourceMapping resourceMapping;
	private Transformer transformerMapping;
	private TemplateEngine templateEngineMapping;
	private SparkResponseTransformer transformer = null;
	private spark.TemplateEngine templateEngine = null;
	private String path;

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
			throw new RegistrationException(getMethod(), "method return type cannot be void");
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

		if (getParent().getResourceMapping() != null && StringUtil.isNotBlank(getParent().getResourceMapping().value())) {
			path = getParent().getResourceMapping().value() + getResourceMapping().value();
		} else {
			path = getResourceMapping().value();
		}
	}

	@Override
	public void register() throws RegistrationException {
		if (transformer != null) {
			transformerRegister();
		} else if (templateEngine != null) {
			templateEngineRegister();
		} else {
			plainRegister();
		}
	}

	private void plainRegister() throws RegistrationException {
		try {
			getParent().getAutoSpark().getAddRoute().invoke(null, getResourceMapping().method().name().toLowerCase(), new RouteImpl(getPath(), getResourceMapping().accepts()) {
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
			});
		} catch (IllegalAccessException | InvocationTargetException e) {
			throw new RegistrationException(getMethod(), "Exception when registering route", e);
		}
	}

	private void transformerRegister() throws RegistrationException {
		try {
			getParent().getAutoSpark().getAddRoute().invoke(null, getResourceMapping().method().name().toLowerCase(), ResponseTransformerRouteImpl.create(
					getPath(),
					getResourceMapping().accepts(),
					new RouteImpl(getPath(), getResourceMapping().accepts()) {
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
	}

	private void templateEngineRegister() throws RegistrationException {
		try {
			getParent().getAutoSpark().getAddRoute().invoke(null, getResourceMapping().method().name().toLowerCase(), TemplateViewRouteImpl.create(
					getPath(),
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

	public String getPath() {
		return path;
	}
}
