package de.faustedition.facsimile;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import de.faustedition.http.HTTP;

import javax.imageio.ImageIO;
import javax.inject.Inject;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/facsimile")
public class FacsimileResource {

    private final FacsimileStore facsimileStore;
    private final ObjectMapper objectMapper;

    @Inject
    public FacsimileResource(FacsimileStore facsimileStore, ObjectMapper objectMapper) {
        this.facsimileStore = facsimileStore;
        this.objectMapper = objectMapper;
    }

    @Path("/metadata/{path: .+?}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public JsonNode metadata(@PathParam("path") final String path) throws IOException {
        final FacsimileMetadata metadata = facsimileStore.metadata(HTTP.normalizePath(path));
        return  objectMapper.createObjectNode()
                .put("width", metadata.getWidth())
                .put("height", metadata.getHeight())
                .put("maxZoom", metadata.getMaxZoom())
                .put("tileSize", metadata.getTileSize());
    }

    @Path("/{path: .+?}")
    @GET
    @Produces("image/jpeg")
    public StreamingOutput tile(@PathParam("path") final String path,
                                @QueryParam("zoom") @DefaultValue("0") final int zoom,
                                @QueryParam("x") @DefaultValue("0") final int x,
                                @QueryParam("y") @DefaultValue("0") final int y) throws IOException {
        return new StreamingOutput() {
            @Override
            public void write(OutputStream output) throws IOException, WebApplicationException {
                ImageIO.write(facsimileStore.tile(HTTP.normalizePath(path), zoom, x, y), "JPEG", output);
            }
        };
    }

    @GET
    public Response index() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
