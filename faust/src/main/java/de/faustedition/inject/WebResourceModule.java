package de.faustedition.inject;

import org.restlet.Context;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

import de.faustedition.template.TemplateConfiguration;
import freemarker.template.Configuration;

public class WebResourceModule extends AbstractModule {

	@Override
	protected void configure() {
		bind(Configuration.class).to(TemplateConfiguration.class);
		bind(Context.class).toProvider(newContextProvider());
	}

	protected Provider<Context> newContextProvider() {
		return new Provider<Context>() {

			@Override
			public Context get() {
				return Context.getCurrent();
			}
		};
	}

}
