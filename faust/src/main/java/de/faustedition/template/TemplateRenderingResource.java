package de.faustedition.template;

import java.io.IOException;

import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class TemplateRenderingResource extends ServerResource {
    private final String contextPath;
    private final TemplateRepresentationFactory viewFactory;

    @Inject
    public TemplateRenderingResource(TemplateRepresentationFactory viewFactory, @Named("ctx.path") String contextPath) {
        this.viewFactory = viewFactory;
        this.contextPath = contextPath;
    }

    @Get
    public TemplateRepresentation render() throws IOException {
        String templatePath = getReference().getPath(true);
        if (templatePath.startsWith(contextPath)) {
            templatePath = templatePath.substring(contextPath.length());
        }
        return viewFactory.create(templatePath, getClientInfo());
    }
}
