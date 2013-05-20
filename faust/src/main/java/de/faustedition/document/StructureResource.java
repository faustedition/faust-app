package de.faustedition.document;

import com.google.common.collect.Maps;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.WebApplication;
import de.faustedition.template.Templates;
import de.faustedition.xml.XMLStorage;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Deque;
import java.util.Map;


@Path("/structure")
@Singleton
public class StructureResource {

	private final XMLStorage xml;
    private final Templates templates;

    @Inject
    public StructureResource(XMLStorage xml, Templates templates) {
        this.xml = xml;
        this.templates = templates;
    }

    @Path("/{path: .+?}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response structure(@PathParam("path") final String path, @Context final javax.ws.rs.core.Request request, @Context final SecurityContext sc) {
        final Deque<String> pathDeque = WebApplication.pathDeque(path);
        pathDeque.addFirst("archival");
        pathDeque.addFirst("structure");

        final FaustURI uri = new FaustURI(FaustAuthority.XML, pathDeque);
        if (!xml.isResource(uri)) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(uri.toString()).build());
        }

        final Map<String, Object> viewModel = Maps.newHashMap();
        viewModel.put("uri", uri);

        return templates.render("structure/structure", viewModel, request, sc);
    }
}
