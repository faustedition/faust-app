package de.faustedition;

import com.google.common.util.concurrent.Service;
import com.google.common.util.concurrent.ServiceManager;
import dagger.Module;
import dagger.ObjectGraph;
import de.faustedition.document.DocumentImageLinkResource;
import de.faustedition.document.DocumentResource;
import de.faustedition.document.Documents;
import de.faustedition.facsimile.FacsimileResource;
import de.faustedition.genesis.GeneticGraphResource;
import de.faustedition.http.HttpService;
import de.faustedition.http.JsonMessageBodyReaderWriter;
import de.faustedition.search.SearchResource;
import de.faustedition.transcript.SceneStatisticsResource;
import de.faustedition.transcript.TranscriptResource;
import de.faustedition.transcript.VerseStatisticsResource;
import de.faustedition.xml.XMLQueryResource;
import de.faustedition.xml.XMLResource;

import java.util.Arrays;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.logging.Level;
import java.util.logging.Logger;

@Module(includes = Infrastructure.class, injects = {
        JsonMessageBodyReaderWriter.class,
        DemoResource.class,
        HomeResource.class,
        ProjectResource.class,
        DocumentResource.class,
        DocumentImageLinkResource.class,
        FacsimileResource.class,
        GeneticGraphResource.class,
        SearchResource.class,
        SceneStatisticsResource.class,
        TranscriptResource.class,
        VerseStatisticsResource.class,
        XMLResource.class,
        XMLQueryResource.class
})
public class Server {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    public static void main(String... args) {
        try {
            final Infrastructure infrastructure = Infrastructure.create(args);
            final Configuration configuration = infrastructure.getConfiguration();
            final ObjectGraph objectGraph = ObjectGraph.create(new Server(), infrastructure);

            final ServiceManager serviceManager = new ServiceManager(Arrays.asList(
                    objectGraph.get(Documents.class),
                    new HttpService(configuration,
                            objectGraph.get(JsonMessageBodyReaderWriter.class),
                            objectGraph.get(DemoResource.class),
                            objectGraph.get(HomeResource.class),
                            objectGraph.get(ProjectResource.class),
                            objectGraph.get(DocumentResource.class),
                            objectGraph.get(DocumentImageLinkResource.class),
                            objectGraph.get(FacsimileResource.class),
                            objectGraph.get(GeneticGraphResource.class),
                            objectGraph.get(SearchResource.class),
                            objectGraph.get(SceneStatisticsResource.class),
                            objectGraph.get(TranscriptResource.class),
                            objectGraph.get(VerseStatisticsResource.class),
                            objectGraph.get(XMLQueryResource.class),
                            objectGraph.get(XMLResource.class)
                    )
            ));
            serviceManager.addListener(START_FAILURE_LISTENER, Executors.newSingleThreadExecutor());

            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    try {
                        serviceManager.stopAsync().awaitStopped(5, TimeUnit.SECONDS);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            });
            serviceManager.startAsync().awaitStopped();
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Fatal error while running server", e);
            System.exit(3);
        }
    }


    private static final ServiceManager.Listener START_FAILURE_LISTENER = new ServiceManager.Listener() {

        public void stopped() {
        }

        public void healthy() {
        }

        public void failure(Service service) {
            System.exit(1);
        }
    };
}
