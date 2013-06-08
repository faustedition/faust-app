package de.faustedition.facsimile;

import de.faustedition.http.WebApplication;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;

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
import java.util.Map;

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
        final FacsimileMetadata metadata = facsimileStore.metadata(WebApplication.path(WebApplication.pathDeque(path)));
        final ObjectNode metadataNode = objectMapper.createObjectNode();
        metadataNode.put("width", metadata.getWidth());
        metadataNode.put("height", metadata.getHeight());
        metadataNode.put("maxZoom", metadata.getMaxZoom());
        metadataNode.put("tileSize", metadata.getTileSize());
        return metadataNode;
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
                ImageIO.write(facsimileStore.tile(WebApplication.path(WebApplication.pathDeque(path)), zoom, x, y), "JPEG", output);
            }
        };
    }

    @GET
    public Response index() {
        return Response.status(Response.Status.NOT_FOUND).build();
    }
}
