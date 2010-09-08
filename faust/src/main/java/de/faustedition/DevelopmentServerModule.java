package de.faustedition;

import de.faustedition.facsimile.FacsimileProxyResource;

public class DevelopmentServerModule extends ServerModule {

    @Override
    protected void configure() {
        super.configure();
        
        bind(FacsimileProxyResource.class);
    }
}
