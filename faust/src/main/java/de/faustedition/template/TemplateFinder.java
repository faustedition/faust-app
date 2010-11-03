package de.faustedition.template;

import java.io.IOException;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Finder;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class TemplateFinder extends Finder {
    private final String contextPath;
    private final TemplateRepresentationFactory viewFactory;

    @Inject
    public TemplateFinder(TemplateRepresentationFactory viewFactory, @Named("ctx.path") String contextPath) {
        this.viewFactory = viewFactory;
        this.contextPath = contextPath;
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
            return viewFactory.create(templatePath, getClientInfo());
        }
    }
}
