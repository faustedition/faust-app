package de.faustedition.xml;

import de.faustedition.http.HTTP;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.xml.transform.Source;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.io.IOException;

@Path("/xml")
@Singleton
public class XMLResource {

    private final Sources sources;

    @Inject
    public XMLResource(Sources sources) {
        this.sources = sources;
    }

    @Path("/{path: .+?}")
    @GET
    @Produces(MediaType.APPLICATION_XML)
    public Source xml(@PathParam("path") final String path) throws IOException {
        final File xmlFile = sources.apply(HTTP.normalizePath(path));
        if (!xmlFile.isFile()) {
            throw new WebApplicationException(javax.ws.rs.core.Response.status(javax.ws.rs.core.Response.Status.NOT_FOUND).entity(path).build());
        }
        return new StreamSource(xmlFile);
    }
}
