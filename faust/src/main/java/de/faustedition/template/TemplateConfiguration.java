package de.faustedition.template;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import com.google.inject.Inject;
import com.google.inject.Singleton;
import com.google.inject.name.Named;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.TemplateModelException;

@Singleton
public class TemplateConfiguration extends Configuration {

	@Inject
	public TemplateConfiguration(@Named("config") Properties configProps, @Named("template.home") String templateDirectory)
			throws TemplateModelException, IOException {
		super();
		setTemplateLoader(new FileTemplateLoader(new File(templateDirectory)));
		setAutoIncludes(Collections.singletonList("/header.ftl"));
		setDefaultEncoding("UTF-8");
		setOutputEncoding("UTF-8");
		setURLEscapingCharset("UTF-8");
		setStrictSyntaxMode(true);
		setWhitespaceStripping(true);
		setObjectWrapper(new TemplateObjectWrapper());

		final Map<String, String> configuration = new HashMap<String, String>();
		for (Enumeration<?> properties = configProps.propertyNames(); properties.hasMoreElements();) {
			final String propertyName = (String) properties.nextElement();
			configuration.put(propertyName, configProps.getProperty(propertyName));
		}
		setSharedVariable("config", configuration);

	}
}
