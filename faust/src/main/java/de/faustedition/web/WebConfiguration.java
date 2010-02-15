package de.faustedition.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.View;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.view.ContentNegotiatingViewResolver;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;
import org.springframework.web.servlet.view.json.MappingJacksonJsonView;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

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

		configurer.setFreemarkerVariables(variables);
		return configurer;
	}

	@Bean
	public ViewResolver htmlViewResolver() {
		FreeMarkerViewResolver htmlViewResolver = new FreeMarkerViewResolver();
		htmlViewResolver.setContentType("application/xhtml+xml;charset=utf-8");
		htmlViewResolver.setPrefix("");
		htmlViewResolver.setSuffix(".ftl");
		return htmlViewResolver;
	}

	@Bean
	public ViewResolver viewResolver() {
		Map<String, String> mediaTypes = Maps.newHashMap();
		mediaTypes.put("html", "application/xhtml+xml;charset=utf-8");
		mediaTypes.put("svg", "image/svg+xml");
		mediaTypes.put("json", "application/json");

		List<ViewResolver> resolverList = Lists.newArrayList();
		resolverList.add(htmlViewResolver());

		List<View> defaultViews = Lists.newArrayList();
		defaultViews.add(new MappingJacksonJsonView());
		defaultViews.add(new Tei2SvgView());

		ContentNegotiatingViewResolver viewResolver = new ContentNegotiatingViewResolver();
		viewResolver.setMediaTypes(mediaTypes);
		viewResolver.setViewResolvers(resolverList);
		viewResolver.setDefaultViews(defaultViews);

		return viewResolver;
	}
}
