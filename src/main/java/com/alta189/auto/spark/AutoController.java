package com.alta189.auto.spark;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.reflections.ReflectionUtils.getAllMethods;
import static org.reflections.ReflectionUtils.withAnnotation;

class AutoController {
	private static final Logger logger = LoggerFactory.getLogger(AutoController.class);
	private final List<Registration> registrations = new ArrayList<>();
	private final AutoSpark autoSpark;
	private final Class<?> controllerClass;
	private final Object controllerInstance;
	private final ResourceMapping resourceMapping;
	private final Transformer transformerMapping;
	private final SparkResponseTransformer transformer;
	private final TemplateEngine templateEngineMapping;
	private final spark.TemplateEngine templateEngine;

	protected AutoController(AutoSpark autoSpark, Class<?> controllerClass) throws RegistrationException {
		this.autoSpark = autoSpark;
		this.controllerClass = controllerClass;

		controllerInstance = AutoSparkUtils.getObjectInstance(getControllerClass());
		if (controllerInstance == null) {
			throw new RegistrationException(getControllerClass(), "Cannot create instance of the controller");
		}

		resourceMapping = getControllerClass().getAnnotation(ResourceMapping.class);
		transformerMapping = getControllerClass().getAnnotation(Transformer.class);
		templateEngineMapping = getControllerClass().getAnnotation(TemplateEngine.class);

		if (getTransformerMapping() != null && !getTransformerMapping().value().equals(DefaultSparkResponseTransformer.class)) {
			transformer = AutoSparkUtils.getObjectInstance(getTransformerMapping().value());
		} else {
			transformer = null;
		}

		if (getTemplateEngineMapping() != null && !getTemplateEngineMapping().value().equals(DefaultTemplateEngine.class)) {
			templateEngine = AutoSparkUtils.getObjectInstance(getTemplateEngineMapping().value());
		} else {
			templateEngine = null;
		}

		if (getTransformer() != null && getTemplateEngine() != null) {
			throw new RegistrationException(getControllerClass(), "Transformer and Template Engine cannot both be set");
		}
	}

	@SuppressWarnings("unchecked")
	public void build() {
		Set<Method> resourceMethods = getAllMethods(controllerClass, withAnnotation(ResourceMapping.class));
		for (Method method : resourceMethods) {
			ResourceRegistration registration = new ResourceRegistration(this, method);
			try {
				registration.build();
				registrations.add(registration);
			} catch (RegistrationException e) {
				logger.error("Exception when building resource mapping", e);
			}
		}
		Set<Method> exceptionMethods = getAllMethods(controllerClass, withAnnotation(ExceptionMapping.class));
		for (Method method : exceptionMethods) {
			ExceptionRegistration registration = new ExceptionRegistration(this, method);
			try {
				registration.build();
				registrations.add(registration);
			} catch (RegistrationException e) {
				logger.error("Exception when building exception mapping", e);
			}
		}
	}

	public void register() {
		getRegistrations().forEach(this::register);
	}

	private void register(Registration registration) {
		try {
			registration.register();
		} catch (RegistrationException e) {
			e.printStackTrace();
		}
	}

	public List<Registration> getRegistrations() {
		return registrations;
	}

	public AutoSpark getAutoSpark() {
		return autoSpark;
	}

	public Class<?> getControllerClass() {
		return controllerClass;
	}

	public Object getControllerInstance() {
		return controllerInstance;
	}

	public ResourceMapping getResourceMapping() {
		return resourceMapping;
	}

	public Transformer getTransformerMapping() {
		return transformerMapping;
	}

	public SparkResponseTransformer getTransformer() {
		return transformer;
	}

	public TemplateEngine getTemplateEngineMapping() {
		return templateEngineMapping;
	}

	public spark.TemplateEngine getTemplateEngine() {
		return templateEngine;
	}
}
