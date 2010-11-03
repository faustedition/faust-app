package de.faustedition.inject;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.matcher.Matchers.only;
import static com.google.inject.matcher.Matchers.subclassesOf;

import org.neo4j.util.GraphDatabaseLifecycle;
import org.restlet.resource.ServerResource;

import com.google.inject.AbstractModule;

import de.faustedition.graph.GraphDatabaseLifecycleProvider;
import de.faustedition.graph.GraphDatabaseTransactionInterceptor;
import de.faustedition.graph.GraphDatabaseTransactional;

public class DataAccessModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(GraphDatabaseLifecycle.class).toProvider(GraphDatabaseLifecycleProvider.class);
        try {
            final GraphDatabaseTransactionInterceptor txInterceptor = new GraphDatabaseTransactionInterceptor();
            requestInjection(txInterceptor);

            bindInterceptor(any(), annotatedWith(GraphDatabaseTransactional.class), txInterceptor);
            bindInterceptor(subclassesOf(ServerResource.class).and(annotatedWith(GraphDatabaseTransactional.class)),
                    only(ServerResource.class.getMethod("handle")), txInterceptor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

    }
}
