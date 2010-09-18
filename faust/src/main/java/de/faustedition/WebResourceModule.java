package de.faustedition;

import org.restlet.Context;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

import de.faustedition.document.ArchiveResource;
import de.faustedition.facsimile.FacsimileProxyResource;
import de.faustedition.genesis.GenesisSampleResource;
import de.faustedition.template.TemplateConfiguration;
import de.faustedition.template.TemplateRenderingResource;
import de.faustedition.template.TemplateRepresentationFactory;
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

        bind(Context.class).toProvider(newContextProvider());

        bind(ArchiveResource.class);
        bind(GenesisSampleResource.class);
        
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
