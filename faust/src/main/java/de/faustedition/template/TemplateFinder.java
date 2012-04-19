package de.faustedition.template;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Finder;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TemplateFinder extends Finder implements InitializingBean {
	@Autowired
	private TemplateRepresentationFactory templateFactory;

	@Autowired
	private Environment environment;

	private String contextPath;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.contextPath = environment.getRequiredProperty("ctx.path");
	}

	@Override
	public ServerResource find(Request request, Response response) {
		return new TemplateRenderingResource();
	}

	public class TemplateRenderingResource extends ServerResource {
		@Get
		public TemplateRepresentation render() throws IOException {
			String templatePath = getReference().getPath(true);
			if (templatePath.startsWith(contextPath)) {
				templatePath = templatePath.substring(contextPath.length() + 1);
			}
			return templateFactory.create(templatePath, getClientInfo());
		}
	}
}
