package de.faustedition;

import com.google.common.base.Throwables;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.env.MutablePropertySources;
import org.springframework.core.env.PropertiesPropertySource;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.support.ResourcePropertySource;
import org.springframework.test.context.support.GenericXmlContextLoader;

import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class CustomContextLoader extends GenericXmlContextLoader {

	@Override
	protected void prepareContext(GenericApplicationContext context) {
		super.prepareContext(context);
		try {
			final MutablePropertySources ps = context.getEnvironment().getPropertySources();
			ps.addLast(new PropertiesPropertySource("system", System.getProperties()));
			ps.addLast(new ResourcePropertySource(new FileSystemResource(System.getProperty("faust.config"))));
			ps.addLast(new ResourcePropertySource("classpath:/config-default.properties"));
		} catch (IOException e) {
			throw Throwables.propagate(e);
		}

	}
}