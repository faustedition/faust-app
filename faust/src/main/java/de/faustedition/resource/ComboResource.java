package de.faustedition.resource;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.base.Predicates;
import com.google.common.base.Strings;
import com.google.common.collect.Iterables;
import com.google.common.io.CharStreams;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;
import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/resources")
public class ComboResource {

	private final TextResourceResolver resolver;

    public ComboResource(String contextPath, File staticDirectory) {
        this.resolver = new TextResourceResolver();
        FileBasedTextResourceCollection.register(resolver, "yui3/", new File(staticDirectory, "yui3"), contextPath + "/static/yui3", Charset.forName("UTF-8"), 86400);
        FileBasedTextResourceCollection.register(resolver, "css/", new File(staticDirectory, "css"), contextPath + "/static/css/", Charset.forName("UTF-8"), 0);
        FileBasedTextResourceCollection.register(resolver, "js/", new File(staticDirectory, "js"), contextPath + "/static/js/", Charset.forName("UTF-8"), 0);
    }


    @GET
    public Response get(@Context UriInfo uriInfo) throws IOException {
        final List<String> parameters = Arrays.asList(Objects.firstNonNull(uriInfo.getRequestUri().getQuery(), "").split("&"));
        final Iterable<String> resources = Iterables.filter(Iterables.transform(parameters, new Function<String, String>() {

            @Override
            public String apply(String input) {
                final int equalsIndex = input.indexOf('=');
                final String parameterName = (equalsIndex < 0 ? input : input.substring(0, equalsIndex));
                return Strings.emptyToNull(parameterName.trim());
            }
        }), Predicates.notNull());

        final TextResourceCombo combo = resolver.resolve(resources);
        return Response.ok()
                .type(combo.getMediaType())
                .lastModified(new Date(combo.lastModified()))
                .expires(new Date(System.currentTimeMillis() + combo.maxAge()))
                .entity(CharStreams.toString(combo))
                .build();
    }
}
