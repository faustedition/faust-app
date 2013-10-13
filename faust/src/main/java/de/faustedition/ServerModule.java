package de.faustedition;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Preconditions;
import dagger.Module;
import dagger.Provides;
import de.faustedition.facsimile.DefaultFacsimiles;
import de.faustedition.facsimile.Facsimiles;
import de.faustedition.facsimile.MockFacsimiles;
import de.faustedition.graph.Graph;
import de.faustedition.text.NamespaceMapping;
import de.faustedition.xml.Sources;
import org.neo4j.kernel.EmbeddedGraphDatabase;

import javax.inject.Singleton;
import java.io.File;
import java.util.ArrayDeque;
import java.util.Collections;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Module(library = true)
public class ServerModule {

    private static final Logger LOG = Logger.getLogger(ServerModule.class.getName());

    private final File dataDirectory;
    private final Configuration configuration;

    public ServerModule(File dataDirectory) {
        this.dataDirectory = dataDirectory;
        this.configuration = Configuration.read(dataDirectory);
    }

    public static ServerModule fromCommandLineArgs(String... args) {
        if (args.length < 1) {
            System.err.println("Usage: java -jar <faust-jar-path> <data-directory-path>");
            System.exit(1);
        }

        final File dataDirectory = new File(args[0]);
        if (!dataDirectory.isDirectory() && !dataDirectory.mkdirs()) {
            LOG.severe("Cannot find or create data directory: " + dataDirectory);
            System.exit(2);
        }

        return new ServerModule(dataDirectory);
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
    public Facsimiles provideFacsimiles() {
        return (facsimilesAvailable() ? new DefaultFacsimiles(dataSubDirectory("facsimile")) : new MockFacsimiles());
    }

    @Provides
    @Singleton
    public Graph provideGraph() {
        final EmbeddedGraphDatabase graphDatabase = new EmbeddedGraphDatabase(new File(dataDirectory, "graph").getPath());
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
    public Sources provideXmlStorage() {
        return new Sources(dataSubDirectory("xml"));
    }

    @Provides
    @Singleton
    public ObjectMapper provideObjectMapper() {
        return new ObjectMapper();
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
