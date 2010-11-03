package de.faustedition.genesis;

import org.restlet.routing.Router;

import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import de.faustedition.InjectorFinder;

@Singleton
public class GenesisRouter extends Router {

    @Inject
    public GenesisRouter(Injector injector) {
        attach("chart.png", new InjectorFinder(injector, GenesisSampleChartResource.class));
        attach("", new InjectorFinder(injector, GenesisSampleResource.class));
    }

}
