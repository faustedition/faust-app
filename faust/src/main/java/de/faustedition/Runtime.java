package de.faustedition;

import com.google.common.collect.Iterables;
import de.faustedition.inject.FaustInjector;
import org.slf4j.bridge.SLF4JBridgeHandler;

import java.io.File;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

public abstract class Runtime {

	public static void main(Class<? extends Runnable> clazz, String[] args) {
		Locale.setDefault(new Locale("en", "us"));
		
		File configFile = null;
		for (String arg : args) {
			if ("-debug".equalsIgnoreCase(arg)) {
				continue;
			} else if (configFile == null) {
				configFile = new File(arg);
			}
		}

		final Logger rootLogger = Logger.getLogger("");
		for (ConsoleHandler ch : Iterables.filter(Arrays.asList(rootLogger.getHandlers()), ConsoleHandler.class)) {
			rootLogger.removeHandler(ch);
		}
		SLF4JBridgeHandler.install();

		FaustInjector.get(configFile).getInstance(clazz).run();
	}
}
