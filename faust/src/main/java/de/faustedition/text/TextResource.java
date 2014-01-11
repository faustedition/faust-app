package de.faustedition.text;

import com.google.common.base.Joiner;
import com.google.common.base.Splitter;
import de.faustedition.Templates;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/text")
@Singleton
public class TextResource {

    private static final Joiner PATH_JOINER = Joiner.on("/").skipNulls();
    private static final Splitter PATH_SPLITTER = Splitter.on("/").omitEmptyStrings();

    private final Texts texts;
    private final Templates templates;

    @Inject
    public TextResource(Texts texts, Templates templates) {
        this.texts = texts;
        this.templates = templates;
    }

    @Path("/{path: .+?}")
    @GET
    public Response view(@Context final Request request, @PathParam("path") String path) {
        final String[] keys = texts.keys();
        final String key = Texts.toKey(PATH_SPLITTER.split(path));

        final int index = Arrays.binarySearch(keys, key);
        if (index < 0) {
            throw new WebApplicationException(path, Response.Status.NOT_FOUND.getStatusCode());
        }

        return templates.render(request, new Templates.ViewAndModel("text")
                .add("key", key)
                .add("next", (index == keys.length - 1 ? null : PATH_JOINER.join(Texts.toPath(keys[index + 1]))))
                .add("prev", (index == 0 ? null : PATH_JOINER.join(Texts.toPath(keys[index - 1]))))
                .add("text", texts.get().get(key))
        );
    }

    @GET
    public Response index(@Context UriInfo uri) {
        return Response.temporaryRedirect(uri.getAbsolutePathBuilder()
                .path(getClass(), "view")
                .buildFromEncoded(PATH_JOINER.join(Texts.toPath(texts.keys()[0])))
        ).build();
    }
}
