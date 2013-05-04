package de.faustedition;

import com.google.common.collect.Lists;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourcePropertySource;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

public abstract class Runtime {

    private static final String CONFIG_FILE_NAME = "faust.properties";

	public static void main(Class<? extends Runnable> clazz, String[] args) throws IOException {
		Locale.setDefault(new Locale("en", "us"));

		final List<File> configFileCandidates = Lists.newLinkedList(Arrays.asList(
                new File(System.getProperty("user.home"), CONFIG_FILE_NAME),
                new File(System.getProperty("user.dir"), CONFIG_FILE_NAME)
        ));
		for (String arg : args) {
			if ("-debug".equalsIgnoreCase(arg)) {
				continue;
			} else {
				configFileCandidates.add(new File(arg));
                break;
			}
		}

		final ClassPathXmlApplicationContext applicationContext = new ClassPathXmlApplicationContext(new String[]{"/application-context.xml"}, false);

		final MutablePropertySources ps = applicationContext.getEnvironment().getPropertySources();
		ps.addLast(new PropertiesPropertySource("system", System.getProperties()));
        for (File configFileCandidate : configFileCandidates) {
            if (configFileCandidate.isFile()) {
                ps.addLast(new ResourcePropertySource(new FileSystemResource(configFileCandidate)));
            }
        }
		ps.addLast(new ResourcePropertySource("classpath:/config-default.properties"));

		applicationContext.registerShutdownHook();
		applicationContext.refresh();
		applicationContext.getBean(clazz).run();
	}
}
