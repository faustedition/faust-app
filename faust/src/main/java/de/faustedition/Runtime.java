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
