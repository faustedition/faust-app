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
@Path("/demo")
@Singleton
public class DemoResource {

    private final Templates templates;

    @Inject
    public DemoResource(Templates templates) {
        this.templates = templates;
    }

    @Path("/{page}")
    @GET
    public Response page(@PathParam("page") final String page, @Context Request request, @Context SecurityContext sc) {
        return templates.render("demo/" + page, Maps.<String, Object>newHashMap(), request, sc);
    }
}
