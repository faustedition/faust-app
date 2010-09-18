package de.faustedition;

import static com.google.inject.matcher.Matchers.annotatedWith;
import static com.google.inject.matcher.Matchers.any;
import static com.google.inject.matcher.Matchers.only;
import static com.google.inject.matcher.Matchers.subclassesOf;

import org.neo4j.util.GraphDatabaseLifecycle;
import org.restlet.resource.ServerResource;

import com.google.inject.AbstractModule;

import de.faustedition.db.GraphDatabaseLifecycleProvider;
import de.faustedition.db.GraphDatabaseRoot;
import de.faustedition.db.GraphDatabaseTransactionInterceptor;
import de.faustedition.db.GraphDatabaseTransactional;
import de.faustedition.document.ArchiveManager;
import de.faustedition.document.DocumentManager;
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

        bind(GraphDatabaseLifecycle.class).toProvider(GraphDatabaseLifecycleProvider.class).asEagerSingleton();
        bind(GraphDatabaseRoot.class).asEagerSingleton();

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
        bind(DocumentManager.class);
        bind(TranscriptManager.class);
    }
}
