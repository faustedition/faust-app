package de.faustedition.inject;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;

import com.google.inject.Injector;

public class InjectorFinder extends Finder {

	private final Injector injector;
	private final Class<? extends ServerResource> clazz;

	public InjectorFinder(Injector injector, Class<? extends ServerResource> clazz) {
		super();
		this.injector = injector;
		this.clazz = clazz;
	}

	@Override
	public ServerResource find(Request request, Response response) {
		return injector.getInstance(clazz);
	}
}
