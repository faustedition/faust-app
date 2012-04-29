package de.faustedition.facsimile;

import com.google.common.collect.Maps;
import net.sf.ehcache.Ehcache;
import net.sf.ehcache.Element;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import javax.imageio.ImageReader;
import java.io.File;
import java.io.IOException;
import java.util.Map;

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
                return new StringRepresentation((String) cacheElement.getValue(), MediaType.APPLICATION_JSON);
            }
            cache.remove(facsimile);
        }

        ImageReader reader = null;
        try {
            reader = facsimile.createImageReader();
            final Map<String, Object> metadata = Maps.newHashMap();
            metadata.put("width", reader.getWidth(0));
            metadata.put("height", reader.getHeight(0));
            metadata.put("maxZoom", reader.getNumImages(true) - 1);
            metadata.put("tileSize", FacsimileTile.SIZE);

            final String metadataStr = OBJECT_MAPPER.writeValueAsString(metadata);
            cache.put(new Element(facsimile, metadataStr));
            return new StringRepresentation(metadataStr, MediaType.APPLICATION_JSON);
        } finally {
            if (reader != null) {
                reader.dispose();
            }
        }
    }
}
