package de.faustedition;

import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.cli.Options;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

public class Server extends AbstractIdleService {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    private final Set<Service> services = Sets.newHashSet();
    private Injector injector;

    /**
     * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Component {
    }

    public static void main(String... args) {
        try {
            final Server server = new Server();
            Runtime.getRuntime().addShutdownHook(new Thread(new Runnable() {
                @Override
                public void run() {
                    server.stopAndWait();
                }
            }));

            server.start();
            synchronized (server) {
                try {
                    server.wait();
                } catch (InterruptedException e) {
                }
            }
        } catch (Exception e) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "Fatal error while running server", e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void startUp() throws Exception {
        injector = Guice.createInjector(
                new ConfigurationModule(),
                new ThreadingModule(),
                new DataStoreModule(),
                new MarshallingModule(),
                new HttpModule()
        );

        final Class<?> thisClass = getClass();
        final Package thisPackage = thisClass.getPackage();
        for (ClassPath.ClassInfo classInfo : ClassPath.from(thisClass.getClassLoader()).getTopLevelClassesRecursive(thisPackage.getName())) {
            Class<?> candidate = classInfo.load();
            if (candidate.isAnnotationPresent(Component.class) && Service.class.isAssignableFrom(candidate)) {
                start((Class<? extends Service>) candidate);
            }
        }
    }

    private void start(Class<? extends Service> serviceClass) {
        if (LOG.isLoggable(Level.INFO)) {
            LOG.log(Level.INFO, "Starting {0}", serviceClass);
        }
        final Service service = injector.getInstance(serviceClass);
        service.start();
        services.add(service);
    }

    @Override
    protected void shutDown() throws Exception {
        try {
            final Set<ListenableFuture<State>> stopFutures = Sets.newHashSet();
            for (Service service : services) {
                if (LOG.isLoggable(Level.INFO)) {
                    LOG.log(Level.INFO, "Stopping {0}", service);
                }
                stopFutures.add(service.stop());
            }
            for (ListenableFuture<State> stopFuture : stopFutures) {
                try {
                    stopFuture.get();
                } catch (InterruptedException e) {
                } catch (ExecutionException e) {
                    if (LOG.isLoggable(Level.SEVERE)) {
                        LOG.log(Level.SEVERE, "Error while stopping service", e);
                    }
                }
            }
        } catch (Exception e) {
            if (LOG.isLoggable(Level.SEVERE)) {
                LOG.log(Level.SEVERE, "Error stopping services", e);
            }
        }
    }

    static final Options OPTIONS = new Options();

    static {
        OPTIONS.addOption("h", "help", false, "print usage instructions (which your are looking at right now)");
        /*
        OPTIONS.addOption("o", "output", true, "output file; '-' for standard output (default)");
        OPTIONS.addOption("ie", "input-encoding", true, "charset to use for decoding non-XML witnesses; default: UTF-8");
        OPTIONS.addOption("oe", "output-encoding", true, "charset to use for encoding the output; default: UTF-8");
        OPTIONS.addOption("xml", "xml-mode", false, "witnesses are treated as XML documents");
        OPTIONS.addOption("xp", "xpath", true, "XPath 1.0 expression evaluating to tokens of XML witnesses; default: '//text()'");
        OPTIONS.addOption("a", "algorithm", true, "progressive alignment algorithm to use 'dekker' (default), 'medite', 'needleman-wunsch'");
        OPTIONS.addOption("t", "tokenized", false, "consecutive matches of tokens will *not* be joined to segments");
        OPTIONS.addOption("f", "format", true, "result/output format: 'json', 'csv', 'dot', 'graphml', 'tei'");
        OPTIONS.addOption("s", "script", true, "ECMA/JavaScript resource with functions to be plugged into the alignment algorithm");
        */
    }
}
