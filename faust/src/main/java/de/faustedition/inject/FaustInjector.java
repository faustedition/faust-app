package de.faustedition.inject;

import com.google.inject.Guice;
import com.google.inject.Injector;
import com.google.inject.Module;
import com.google.inject.Stage;

import de.faustedition.RuntimeMode;

public class FaustInjector {

	private static Injector injector = null;

	public static Injector get() {
		synchronized (FaustInjector.class) {
			if (injector == null) {
				ConfigurationModule configurationModule = new ConfigurationModule();
				Stage stage = (configurationModule.mode == RuntimeMode.PRODUCTION ? Stage.PRODUCTION
						: Stage.DEVELOPMENT);
				injector = Guice.createInjector(stage, new Module[] { configurationModule, new DataAccessModule(),
						new WebResourceModule() });

			}
		}
		return injector;
	}
}
