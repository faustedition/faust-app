package de.faustedition.http;

import com.google.common.base.Objects;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.container.ContainerRequestContext;
import javax.ws.rs.container.ContainerResponseContext;
import javax.ws.rs.container.ContainerResponseFilter;
import javax.ws.rs.core.MultivaluedMap;
import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class CrossOriginResourceSharingContainerFilter implements ContainerResponseFilter {

    @Inject
    public CrossOriginResourceSharingContainerFilter() {
    }

    @Override
    public void filter(ContainerRequestContext requestContext, ContainerResponseContext responseContext) throws IOException {
        final MultivaluedMap<String, String> requestHeaders = requestContext.getHeaders();
        final MultivaluedMap<String,Object> responseHeaders = responseContext.getHeaders();

        responseHeaders.add("Access-Control-Allow-Origin", Objects.firstNonNull(requestHeaders.getFirst("Origin"), "*"));
        responseHeaders.add("Access-Control-Allow-Methods", Objects.firstNonNull(requestHeaders.getFirst("Access-Control-Request-Method"), "GET, POST, PUT, DELETE, HEAD, OPTIONS"));
        responseHeaders.add("Access-Control-Allow-Headers", Objects.firstNonNull(requestHeaders.getFirst("Access-Control-Request-Headers"), "Content-Type, Accept, X-Requested-With"));
        responseHeaders.add("Access-Control-Max-Age", "86400");
        responseHeaders.add("Access-Control-Allow-Credentials", "true");
    }
}
