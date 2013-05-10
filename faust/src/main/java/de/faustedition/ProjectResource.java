package de.faustedition;

import com.google.common.collect.Maps;
import de.faustedition.template.Templates;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/project")
@Singleton
public class ProjectResource {

    private final Templates templates;

    @Inject
    public ProjectResource(Templates templates) {
        this.templates = templates;
    }

    @GET
    @Path("/{page}")
    public Response page(@PathParam("page") String page, @Context Request request, @Context SecurityContext sc) {
        return templates.render("project/" + page, Maps.<String,Object>newHashMap(), request, sc);
    }
}
