package de.faustedition.inject;

import org.restlet.Context;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

import de.faustedition.DeploymentMode;
import de.faustedition.document.ArchiveResource;
import de.faustedition.document.DocumentResource;
import de.faustedition.facsimile.FacsimileProxyResource;
import de.faustedition.genesis.GenesisSampleResource;
import de.faustedition.security.LdapSecurityStore;
import de.faustedition.template.TemplateConfiguration;
import de.faustedition.template.TemplateRenderingResource;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.transcript.TranscriptResource;
import freemarker.template.Configuration;

public class WebResourceModule extends AbstractModule {

    private final DeploymentMode mode;

    public WebResourceModule(DeploymentMode mode) {
        super();
        this.mode = mode;
    }

    @Override
    protected void configure() {
        bind(Configuration.class).to(TemplateConfiguration.class);
        bind(TemplateRepresentationFactory.class);
        bind(TemplateRenderingResource.class);

        bind(LdapSecurityStore.class);
        
        bind(Context.class).toProvider(newContextProvider());

        bind(ArchiveResource.class);
        bind(DocumentResource.class);
        bind(GenesisSampleResource.class);
        bind(TranscriptResource.class);
        
        if (mode == DeploymentMode.DEVELOPMENT) {
            bind(FacsimileProxyResource.class);
        }
    }

    protected Provider<Context> newContextProvider() {
        return new Provider<Context>() {

            @Override
            public Context get() {
                return Context.getCurrent();
            }
        };
    }

}
