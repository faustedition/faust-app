package de.faustedition.inject;

import org.restlet.Context;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;

import de.faustedition.document.ArchiveResource;
import de.faustedition.document.DocumentFinder;
import de.faustedition.document.DocumentResource;
import de.faustedition.genesis.GenesisSampleResource;
import de.faustedition.security.LdapSecurityStore;
import de.faustedition.template.TemplateConfiguration;
import de.faustedition.template.TemplateRenderingResource;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.transcript.TranscriptFinder;
import de.faustedition.transcript.TranscriptResource;
import freemarker.template.Configuration;

public class WebResourceModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(Configuration.class).to(TemplateConfiguration.class);
        bind(TemplateRepresentationFactory.class);
        bind(TemplateRenderingResource.class);

        bind(LdapSecurityStore.class);
        
        bind(Context.class).toProvider(newContextProvider());

        bind(ArchiveResource.class);

        bind(DocumentFinder.class);
        bind(DocumentResource.class);

        bind(GenesisSampleResource.class);
        
        bind(TranscriptFinder.class);   
        bind(TranscriptResource.class);        
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
