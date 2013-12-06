package de.faustedition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import dagger.Module;
import dagger.Provides;
import de.faustedition.document.Documents;
import de.faustedition.facsimile.DefaultFacsimiles;
import de.faustedition.facsimile.Facsimiles;
import de.faustedition.facsimile.MockFacsimiles;
import de.faustedition.graph.Graph;
import de.faustedition.index.DocumentIndexer;
import de.faustedition.index.Index;
import de.faustedition.index.TranscriptTokenAnnotationCodec;
import de.faustedition.text.NamespaceMapping;
import de.faustedition.transcript.TranscriptSegments;
import de.faustedition.transcript.Transcripts;
import de.faustedition.xml.Sources;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;

import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import java.util.concurrent.Executors;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Module(injects = {
        Configuration.class,
        EventBus.class,
        NamespaceMapping.class,
        ObjectMapper.class,
        Database.class,
        Graph.class,
        Index.class,
        DocumentIndexer.class,
        Sources.class,
        Facsimiles.class,
        Documents.class,
        Transcripts.class,
        TranscriptSegments.class,
        TranscriptTokenAnnotationCodec.class
}, library = true)
public class Infrastructure {

    private static final Logger LOG = Logger.getLogger(Infrastructure.class.getName());

    private final File dataDirectory;
    private final Configuration configuration;

    public Infrastructure(File dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.configuration = Configuration.read(dataDirectory);
    }

    public static Infrastructure create(String... args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar <faust-jar-path> <data-directory-path>");
            System.exit(1);
        }

        final File dataDirectory = new File(args[0]);
        if (!dataDirectory.isDirectory() && !dataDirectory.mkdirs()) {
            LOG.severe("Cannot find or create data directory: " + dataDirectory);
            System.exit(2);
        }

        return new Infrastructure(dataDirectory);
    }

    public File getDataDirectory() {
        return dataDirectory;
    }

    @Provides
    public Configuration getConfiguration() {
        return configuration;
    }

    @Provides
    @Singleton
    public Database provideDatabase() {
        return new Database(dataDirectory);
    }

    @Provides
    @Singleton
    public Index provideIndex() {
        return new Index(dataSubDirectory("index"));
    }

    @Provides
    @Singleton
    public Facsimiles provideFacsimiles() {
        return (facsimilesAvailable() ? new DefaultFacsimiles(dataSubDirectory("facsimile")) : new MockFacsimiles());
    }

    @Provides
    @Singleton
    public EventBus provideEventBus() {
        return new AsyncEventBus(Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors()));
    }

    @Provides
    @Singleton
    public Graph provideGraph() {
        final GraphDatabaseService graphDatabase = new GraphDatabaseFactory().newEmbeddedDatabase(new File(dataDirectory, "graph").getPath());
        Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
            @Override
            public void run() {
                if (LOG.isLoggable(Level.FINE)) {
                    LOG.fine("Shutting down graph database " + graphDatabase);
                }
                graphDatabase.shutdown();
            }
        }));
        return new Graph(graphDatabase);
    }

    @Provides
    @Singleton
    public Sources provideSources() {
        return new Sources(dataSubDirectory("xml"));
    }

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper().registerModule(new Templates.TemplateModule());
    }

    @Provides
    @Singleton
    public NamespaceMapping provideNamespaceMapping() {
        return new NamespaceMapping();
    }

    protected File dataSubDirectory(String name) {
        final File subDirectory = new File(dataDirectory, name);
        Preconditions.checkArgument(subDirectory.isDirectory() || subDirectory.mkdirs(), subDirectory + " is not a directory");
        return subDirectory;
    }

    protected boolean facsimilesAvailable() {
        final Queue<File> directories = new ArrayDeque<File>(Collections.singleton(dataSubDirectory("facsimile")));
        while (!directories.isEmpty()) {
            for (File file : directories.remove().listFiles()) {
                if (file.isDirectory()) {
                    directories.add(file);
                } else if (file.isFile() && file.getName().endsWith(Facsimiles.IMAGE_FILE_EXTENSION)) {
                    return true;
                }
            }
        }
        return false;
    }


}
