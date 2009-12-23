package de.faustedition.web;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.HandlerAdapter;
import org.springframework.web.servlet.HandlerMapping;
import org.springframework.web.servlet.LocaleResolver;
import org.springframework.web.servlet.ViewResolver;
import org.springframework.web.servlet.i18n.AcceptHeaderLocaleResolver;
import org.springframework.web.servlet.mvc.annotation.AnnotationMethodHandlerAdapter;
import org.springframework.web.servlet.mvc.annotation.DefaultAnnotationHandlerMapping;
import org.springframework.web.servlet.view.freemarker.FreeMarkerViewResolver;

@Configuration
public class WebConfiguration
{
	@Bean
	public HandlerAdapter handlerAdapter()
	{
		return new AnnotationMethodHandlerAdapter();
	}

	@Bean
	public HandlerMapping handlerMapping()
	{
		DefaultAnnotationHandlerMapping handlerMapping = new DefaultAnnotationHandlerMapping();
		handlerMapping.setInterceptors(new Object[] { transactionContextHandlerInterceptor() });
		return handlerMapping;
	}

	@Bean
	public TransactionContextHandlerInterceptor transactionContextHandlerInterceptor()
	{
		return new TransactionContextHandlerInterceptor();
	}

	@Bean
	public LocaleResolver localeResolver()
	{
		return new AcceptHeaderLocaleResolver();
	}

	@Bean
	public ViewResolver viewResolver()
	{
		FreeMarkerViewResolver viewResolver = new FreeMarkerViewResolver();
		viewResolver.setContentType("application/xhtml+xml");
		viewResolver.setPrefix("");
		viewResolver.setSuffix(".ftl");
		return viewResolver;
	}
}
