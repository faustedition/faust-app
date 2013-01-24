package de.faustedition;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.springframework.context.ApplicationContext;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class ApplicationContextFinder<T extends ServerResource> extends Finder {

	private final ApplicationContext applicationContext;
	private final Class<T> beanType;

	public ApplicationContextFinder(ApplicationContext applicationContext, Class<T> beanType) {
		this.applicationContext = applicationContext;
		this.beanType = beanType;
	}

	@Override
	public ServerResource find(Request request, Response response) {
		return applicationContext.getBean(beanType);
	}
}
