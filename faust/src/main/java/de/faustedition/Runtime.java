package de.faustedition;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Locale;
import java.util.logging.ConsoleHandler;
import java.util.logging.Logger;

import org.slf4j.bridge.SLF4JBridgeHandler;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourcePropertySource;

import com.google.common.collect.Iterables;

public abstract class Runtime {

	public static void main(Class<? extends Runnable> clazz, String[] args) throws IOException {
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

		final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[]{"/application-context.xml"}, false);

		final MutablePropertySources ps = applicationContext.getEnvironment().getPropertySources();
		ps.addLast(new PropertiesPropertySource("system", System.getProperties()));
		if (configFile != null) {
			ps.addLast(new ResourcePropertySource(new FileSystemResource(configFile)));
		}
		ps.addLast(new ResourcePropertySource("classpath:/config-default.properties"));

		applicationContext.registerShutdownHook();
		applicationContext.refresh();
		applicationContext.getBean(clazz).run();
	}
}
