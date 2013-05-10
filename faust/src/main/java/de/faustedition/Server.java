package de.faustedition;

import com.google.common.collect.Sets;
import com.google.common.reflect.ClassPath;
import com.google.common.util.concurrent.AbstractIdleService;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.Service;
import com.google.inject.Guice;
import com.google.inject.Injector;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.Locale;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.logging.Level;
import java.util.logging.Logger;

@org.springframework.stereotype.Component
public class Server extends AbstractIdleService {

    private static final Logger LOG = Logger.getLogger(Server.class.getName());

    private Set<Service> services = Sets.newHashSet();
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
}
