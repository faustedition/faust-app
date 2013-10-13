package de.faustedition.document;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Templates;
import de.faustedition.http.HTTP;
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
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.Deque;


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
    public Response structure(@PathParam("path") final String path, @Context final Request request) {
        final Deque<String> pathDeque = HTTP.pathDeque(path);
        pathDeque.addFirst("archival");
        pathDeque.addFirst("structure");

        final FaustURI uri = new FaustURI(FaustAuthority.XML, HTTP.joinPath(pathDeque));
        if (!xml.isResource(uri)) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(uri.toString()).build());
        }

        return templates.render(new Templates.ViewAndModel("structure/structure").add("uri", uri), request);
    }
}
