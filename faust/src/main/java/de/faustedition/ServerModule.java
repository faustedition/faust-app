package de.faustedition;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.InetAddress;
import java.util.Properties;

import org.neo4j.util.GraphDatabaseLifecycle;
import org.restlet.Context;

import com.google.inject.AbstractModule;
import com.google.inject.Provider;
import com.google.inject.name.Names;

import de.faustedition.db.GraphDatabaseLifecycleProvider;
import de.faustedition.document.ArchiveResource;
import de.faustedition.template.TemplateConfiguration;
import de.faustedition.template.TemplateRenderingResource;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.xml.XmlDatabase;
import freemarker.template.Configuration;

public class ServerModule extends AbstractModule {

    @Override
    protected void configure() {
        final Properties configuration = loadConfiguration();
        Names.bindProperties(binder(), configuration);
        bind(Properties.class).annotatedWith(Names.named("config")).toInstance(configuration);

        bind(XmlDatabase.class);
        bind(GraphDatabaseLifecycle.class).toProvider(GraphDatabaseLifecycleProvider.class).asEagerSingleton();               
        
        bind(Configuration.class).to(TemplateConfiguration.class);
        bind(TemplateRepresentationFactory.class);
        bind(TemplateRenderingResource.class);

        bind(Context.class).toProvider(newContextProvider());
        
        bind(ArchiveResource.class);
    }

    protected Provider<Context> newContextProvider() {
        return new Provider<Context>() {

            @Override
            public Context get() {
                return Context.getCurrent();
            }
        };
    }

    protected Properties loadConfiguration() {
        try {
            Properties configuration = new Properties();
            InputStream configStream = getClass().getResourceAsStream("/config-default.properties");
            InputStreamReader configReader = new InputStreamReader(configStream, "UTF-8");
            try {
                configuration.load(configReader);
            } finally {
                configReader.close();
            }

            final String hostName = InetAddress.getLocalHost().getHostName();
            configStream = getClass().getResourceAsStream("/config-" + hostName + ".properties");
            if (configStream != null) {
                configReader = new InputStreamReader(configStream, "UTF-8");
                try {
                    configuration = new Properties(configuration);
                    configuration.load(configReader);
                } finally {
                    configReader.close();
                }
            }
            return configuration;
        } catch (IOException e) {
            throw Log.fatalError(e);
        }
    }

}
