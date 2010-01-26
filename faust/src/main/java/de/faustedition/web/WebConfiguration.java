package de.faustedition.web;

import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

import de.faustedition.model.security.HasRoleTemplateMethod;
import de.faustedition.web.document.Tei2XhtmlTransformer;

@Configuration
public class WebConfiguration {

	@Bean
	public LocaleResolver localeResolver() {
		return new AcceptHeaderLocaleResolver();
	}
	
	@Bean
	public FreeMarkerConfigurer freemarkerConfigurer() {
		FreeMarkerConfigurer configurer = new FreeMarkerConfigurer();
		configurer.setTemplateLoaderPath("/WEB-INF/freemarker");

		Properties settings = new Properties();
		settings.put("auto_include", "/header.ftl");
		settings.put("default_encoding", "UTF-8");
		settings.put("output_encoding", "UTF-8");
		settings.put("url_escaping_charset", "UTF-8");
		settings.put("strict_syntax", "true");
		settings.put("whitespace_stripping", "true");
		configurer.setFreemarkerSettings(settings);

		Map<String, Object> variables = new HashMap<String, Object>();
		variables.put("hasRole", new HasRoleTemplateMethod());
		variables.put("encodePath", new URLPathEncoder());
		variables.put("tei2xhtml", new Tei2XhtmlTransformer());

		configurer.setFreemarkerVariables(variables);
		return configurer;
	}

	@Bean
	public ViewResolver viewResolver() {
		FreeMarkerViewResolver viewResolver = new FreeMarkerViewResolver();
		viewResolver.setContentType("application/xhtml+xml");
		viewResolver.setPrefix("");
		viewResolver.setSuffix(".ftl");
		return viewResolver;
	}
}
