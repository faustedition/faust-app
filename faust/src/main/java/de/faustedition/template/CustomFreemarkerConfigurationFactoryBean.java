/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.template;

import java.io.File;
import java.util.Collections;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.AbstractFactoryBean;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import freemarker.cache.FileTemplateLoader;
import freemarker.template.Configuration;

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
