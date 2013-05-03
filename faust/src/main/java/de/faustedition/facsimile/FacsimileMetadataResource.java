package de.faustedition.facsimile;

import java.io.IOException;
import java.util.Date;
import java.util.Map;

import javax.imageio.ImageReader;

import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;

import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.common.collect.Maps;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public class FacsimileMetadataResource extends ServerResource {
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final Ehcache cache;
    private final FacsimileTile facsimile;

    public FacsimileMetadataResource(Ehcache cache, FacsimileTile facsimile) {
        this.cache = cache;
        this.facsimile = facsimile;
    }

    @Get("json")
    public Representation metadata() throws IOException {
        final Element cacheElement = cache.get(facsimile);
        if (cacheElement != null) {
            if (cacheElement.getCreationTime() >= facsimile.getFile().lastModified()) {
                return toRepresentation((String) cacheElement.getValue());
            }
            cache.remove(facsimile);
        }

        ImageReader reader = null;
        try {
            reader = facsimile.createImageReader();
            final Map<String, Object> metadataMap = Maps.newHashMap();
            metadataMap.put("width", reader.getWidth(0));
            metadataMap.put("height", reader.getHeight(0));
            metadataMap.put("maxZoom", reader.getNumImages(true) - 1);
            metadataMap.put("tileSize", FacsimileTile.SIZE);

            final String metadata = OBJECT_MAPPER.writeValueAsString(metadataMap);
            cache.put(new Element(facsimile, metadata));
            return toRepresentation(metadata);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }

    private StringRepresentation toRepresentation(String metadata) {
        final StringRepresentation representation = new StringRepresentation(metadata, MediaType.APPLICATION_JSON);
        representation.setModificationDate(new Date(facsimile.getFile().lastModified()));
        return representation;
    }
}
