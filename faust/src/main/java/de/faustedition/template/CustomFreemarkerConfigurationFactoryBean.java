package de.faustedition.template;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.Collections;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
public class CustomFreemarkerConfigurationFactoryBean extends AbstractFactoryBean<Configuration> {

	@Autowired
	private Environment environment;

	@Override
	public Class<?> getObjectType() {
		return Configuration.class;
	}

	@Override
	protected Configuration createInstance() throws Exception {
		final Configuration conf = new Configuration();

		conf.setTemplateLoader(new FileTemplateLoader(environment.getRequiredProperty("template.home", File.class)));
		conf.setAutoIncludes(Collections.singletonList("/header.ftl"));
		conf.setDefaultEncoding("UTF-8");
		conf.setOutputEncoding("UTF-8");
		conf.setURLEscapingCharset("UTF-8");
		conf.setStrictSyntaxMode(true);
		conf.setWhitespaceStripping(true);
		conf.setObjectWrapper(new TemplateObjectWrapper());

		conf.setSharedVariable("cp", environment.getRequiredProperty("ctx.path"));
		conf.setSharedVariable("facsimilieIIPUrl", environment.getRequiredProperty("facsimile.iip.url"));
		return conf;
	}
}
