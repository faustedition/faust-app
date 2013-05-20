package de.faustedition.http;

import com.google.common.base.Objects;
import com.sun.jersey.spi.container.ContainerRequest;
import com.sun.jersey.spi.container.ContainerResponse;
import com.sun.jersey.spi.container.ContainerResponseFilter;

import javax.ws.rs.core.MultivaluedMap;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CrossOriginResourceSharingContainerFilter implements ContainerResponseFilter {

    @Override
    public ContainerResponse filter(ContainerRequest request, ContainerResponse response) {
        final MultivaluedMap<String, String> requestHeaders = request.getRequestHeaders();

        final MultivaluedMap<String,Object> responseHeaders = response.getHttpHeaders();
        responseHeaders.add("Access-Control-Allow-Origin", Objects.firstNonNull(requestHeaders.getFirst("Origin"), "*"));
        responseHeaders.add("Access-Control-Allow-Methods", Objects.firstNonNull(requestHeaders.getFirst("Access-Control-Request-Method"), "GET, POST, PUT, DELETE, HEAD, OPTIONS"));
        responseHeaders.add("Access-Control-Allow-Headers", Objects.firstNonNull(requestHeaders.getFirst("Access-Control-Request-Headers"), "Content-Type, Accept, X-Requested-With"));
        responseHeaders.add("Access-Control-Max-Age", "86400");
        responseHeaders.add("Access-Control-Allow-Credentials", "true");

        return response;
    }
}