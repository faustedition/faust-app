package de.faustedition.facsimile;

import org.restlet.data.ChallengeScheme;
import org.restlet.data.Reference;
import org.restlet.representation.Representation;
import org.restlet.resource.ClientResource;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;

import com.google.inject.Inject;
import com.google.inject.name.Named;

public class FacsimileProxyResource extends ServerResource {
    private final String serverUrl;
    private final String serverUser;
    private final String serverPassword;

    @Inject
    public FacsimileProxyResource(@Named("facsimile.iip.server.url") String serverUrl,
            @Named("facsimile.iip.server.user") String serverUser, @Named("facsimile.iip.server.password") String serverPassword) {
        this.serverUrl = serverUrl;
        this.serverUser = serverUser;
        this.serverPassword = serverPassword;
    }

    @Get
    public Representation proxyImageServer() {
        Reference iipServerRef = new Reference(serverUrl);
        iipServerRef.setQuery(getReference().getQuery());

        ClientResource iipServer = new ClientResource(iipServerRef);
        iipServer.setChallengeResponse(ChallengeScheme.HTTP_BASIC, serverUser, serverPassword);

        try {
            return iipServer.get();
        } finally {
            iipServer.release();
        }
    }
}
