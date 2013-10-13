package de.faustedition.xml;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.http.HTTP;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;

@Path("/xml")
@Singleton
public class XMLResource {

	private final Sources xml;

    @Inject
    public XMLResource(Sources xml) {
        this.xml = xml;
    }

    @Path("/{path: .+?}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public SAXSource xml(@PathParam("path") final String path) throws IOException {
        final FaustURI uri = new FaustURI(FaustAuthority.XML, HTTP.normalizePath(path));
        if (!xml.isResource(uri)) {
            throw new WebApplicationException(javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity(uri.toString()).build());
        }
        return new SAXSource(xml.getInputSource(uri));
    }
}
