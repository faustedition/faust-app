package de.faustedition;


import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.ws.rs.core.UriInfo;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/")
@Singleton
public class HomeResource {

    @GET
    public Response start(@Context UriInfo uriInfo, @Context SecurityContext sc) {
        return Response.temporaryRedirect(uriInfo.getBaseUriBuilder()
                .path(User.ANONYMOUS.equals(sc.getUserPrincipal()) ? "project/about" : "archive")
                .build()
        ).build();
    }

    @Path("/login")
    @GET
    public Response login(@Context UriInfo uriInfo, @Context SecurityContext sc) {
        return Response.temporaryRedirect(uriInfo.getBaseUriBuilder().path("archive").build()).build();
    }

}
