package de.faustedition;

import com.google.common.base.Joiner;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;
import com.google.common.reflect.ClassPath;
import com.google.inject.AbstractModule;
import com.google.inject.Injector;
import com.google.inject.Provides;
import com.sun.jersey.api.client.config.ClientConfig;
import com.sun.jersey.api.client.config.DefaultClientConfig;
import com.sun.jersey.api.core.DefaultResourceConfig;
import com.sun.jersey.api.core.ResourceConfig;
import com.sun.jersey.server.impl.container.filter.NormalizeFilter;
import de.faustedition.http.CrossOriginResourceSharingContainerFilter;
import de.faustedition.http.ObjectMapperMessageBodyReaderWriter;

import javax.inject.Singleton;
import javax.ws.rs.ext.Provider;
import java.util.Arrays;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class HttpModule extends AbstractModule {

    private static final Logger LOG = Logger.getLogger(HttpModule.class.getName());

    @Override
    protected void configure() {
    }

    @Provides
    @Singleton
    public ResourceConfig resourceConfig(Injector injector) throws Exception {
        final DefaultResourceConfig rc = new DefaultResourceConfig();

        final Map<String,Object> config = Maps.newHashMap();
        config.put(ResourceConfig.PROPERTY_CONTAINER_REQUEST_FILTERS, Arrays.asList(
                new NormalizeFilter(),
                injector.getInstance(SecurityRequestFilter.class)
        ));
        config.put(ResourceConfig.PROPERTY_CONTAINER_RESPONSE_FILTERS, Arrays.asList(
                new CrossOriginResourceSharingContainerFilter()
        ));
        config.put(ResourceConfig.FEATURE_CANONICALIZE_URI_PATH, true);
        config.put(ResourceConfig.FEATURE_NORMALIZE_URI, true);

        rc.setPropertiesAndFeatures(config);

        final Set<Object> singletons = rc.getSingletons();
        final Class<?> thisClass = getClass();
        final Package thisPackage = thisClass.getPackage();
        final Map<String, Class<?>> resourceMappings = Maps.newTreeMap();
        for (ClassPath.ClassInfo classInfo : ClassPath.from(thisClass.getClassLoader()).getTopLevelClassesRecursive(thisPackage.getName())) {
            Class<?> candidate = classInfo.load();
            if (candidate.isAnnotationPresent(javax.ws.rs.Path.class)) {
                final String path = candidate.getAnnotation(javax.ws.rs.Path.class).value();
                singletons.add(injector.getInstance(candidate));
                Preconditions.checkState(resourceMappings.put(path, candidate) == null, path);
            } else if (candidate.isAnnotationPresent(Provider.class)) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "Adding JAX-RS provider {0}", candidate);
                }
                singletons.add(injector.getInstance(candidate));
            }
        }

        if (LOG.isLoggable(Level.INFO) && !resourceMappings.isEmpty()) {
            int maxPathLength = 0;
            for (String path : resourceMappings.keySet()) {
                maxPathLength = Math.max(maxPathLength, path.length());
            }
            for (Map.Entry<String, Class<?>> mapping : resourceMappings.entrySet()) {
                LOG.log(Level.INFO, "JAX-RS Resource Mapping: " + Joiner.on(" => ").join(
                        Strings.padEnd(mapping.getKey(), maxPathLength, ' '),
                        mapping.getValue())
                );
            }
        }

        //singletons.add(new InstrumentedResourceMethodDispatchAdapter());

        return rc;
    }

    @Provides
    @Singleton
    public ClientConfig clientConfig(ObjectMapperMessageBodyReaderWriter jsonReaderWriter) {
        final DefaultClientConfig cc = new DefaultClientConfig();
        cc.getSingletons().add(jsonReaderWriter);
        return cc;
    }
}
