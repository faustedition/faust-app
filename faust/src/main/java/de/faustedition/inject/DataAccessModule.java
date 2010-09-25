package de.faustedition.inject;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.matcher.Matchers.only;
import static com.google.inject.matcher.Matchers.subclassesOf;

import org.neo4j.util.GraphDatabaseLifecycle;
import org.restlet.resource.ServerResource;

import com.google.inject.AbstractModule;

import de.faustedition.document.ArchiveManager;
import de.faustedition.document.MaterialUnitManager;
import de.faustedition.graph.GraphDatabaseLifecycleProvider;
import de.faustedition.graph.GraphReference;
import de.faustedition.graph.GraphDatabaseTransactionInterceptor;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.tei.TeiValidator;
import de.faustedition.transcript.TranscriptManager;
import de.faustedition.xml.XMLDatabase;
import de.faustedition.xml.XMLStorage;

public class DataAccessModule extends AbstractModule {

    @Override
    protected void configure() {
        bind(XMLStorage.class);
        bind(XMLDatabase.class);
        bind(TeiValidator.class);

        bind(GraphDatabaseLifecycle.class).toProvider(GraphDatabaseLifecycleProvider.class);
        bind(GraphReference.class).asEagerSingleton();

        try {
            final GraphDatabaseTransactionInterceptor txInterceptor = new GraphDatabaseTransactionInterceptor();
            requestInjection(txInterceptor);

            bindInterceptor(any(), annotatedWith(GraphDatabaseTransactional.class), txInterceptor);
            bindInterceptor(subclassesOf(ServerResource.class).and(annotatedWith(GraphDatabaseTransactional.class)),
                    only(ServerResource.class.getMethod("handle")), txInterceptor);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        bind(ArchiveManager.class);
        bind(MaterialUnitManager.class);
        bind(TranscriptManager.class);
    }
}
