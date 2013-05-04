package de.faustedition;

import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;

import java.io.IOException;
import java.util.Locale;

public abstract class Runtime {

	public static void main(Class<? extends Runnable> clazz, String[] args) throws IOException {
		Locale.setDefault(new Locale("en", "us"));

		final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[]{
                "/application-context.xml"
        }, false);

		final MutablePropertySources ps = applicationContext.getEnvironment().getPropertySources();
        ps.addLast(new PropertiesPropertySource("config", Configuration.read()));


		applicationContext.registerShutdownHook();
		applicationContext.refresh();
		applicationContext.getBean(clazz).run();
	}
}
