package de.faustedition;

import com.google.common.base.Preconditions;
import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;
import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.GnuParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Options;

import java.io.File;
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

    private final String contextPath;
    private final int httpPort;
    private final boolean development;

    private final File dataDirectory;
    private final File staticDirectory;
    private final File templateDirectory;

    private final Set<Service> services = Sets.newHashSet();

    private Injector injector;

    public Server(CommandLine commandLine) {
        this.contextPath = commandLine.getOptionValue("cp", "");
        this.httpPort = Integer.parseInt(commandLine.getOptionValue("p", "8080"));
        this.development = commandLine.hasOption("n");

        this.dataDirectory = new File(commandLine.getOptionValue("d", "data"));
        this.staticDirectory = new File(System.getProperty("faust.static", "static"));
        this.templateDirectory = new File(System.getProperty("faust.templates", "templates"));

        Preconditions.checkArgument(dataDirectory.isDirectory(), dataDirectory + " is not a directory");
        Preconditions.checkArgument(staticDirectory.isDirectory(), staticDirectory + " is not a directory");
        Preconditions.checkArgument(templateDirectory.isDirectory(), templateDirectory + " is not a directory");
    }

    /**
     * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
     */
    @Target(ElementType.TYPE)
    @Retention(RetentionPolicy.RUNTIME)
    public @interface Component {
    }

    public static void main(String... args) {
        try {
            final CommandLine commandLine = new GnuParser().parse(OPTIONS, args);
            if (commandLine.hasOption("h")) {
                new HelpFormatter().printHelp(78, "faust-server [<options>]", "", OPTIONS, "");
                return;
            }

            final Server server = new Server(commandLine);
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
                new ThreadingModule(),
                new MarshallingModule(),
                new DataModule(dataDirectory),
                new HttpModule(httpPort, contextPath, staticDirectory, templateDirectory, development)
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
        OPTIONS.addOption("h", "help", false, "print usage instructions");
        OPTIONS.addOption("cp", "context-path", true, "URL context path under which to serve the edition; default: ''");
        OPTIONS.addOption("p", "port", true, "port on which the server listens for HTTP requests; default: 8080");
        OPTIONS.addOption("d", "data", true, "Path to data directory; default: 'data'");
        OPTIONS.addOption("n", "dev", false, "Development mode; disable LDAP authentication etc.");
    }
}
