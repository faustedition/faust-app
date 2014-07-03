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

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.ext.freemarker.TemplateRepresentation;
import org.restlet.resource.Finder;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TemplateFinder extends Finder implements InitializingBean {
	@Autowired
	private TemplateRepresentationFactory templateFactory;

	@Autowired
	private Environment environment;

	private String contextPath;

	@Override
	public void afterPropertiesSet() throws Exception {
		this.contextPath = environment.getRequiredProperty("ctx.path");
	}

	@Override
	public ServerResource find(Request request, Response response) {
		return new TemplateRenderingResource();
	}

	public class TemplateRenderingResource extends ServerResource {
		@Get
		public TemplateRepresentation render() throws IOException {
			String templatePath = getReference().getPath(true);
			if (templatePath.startsWith(contextPath)) {
				templatePath = templatePath.substring(contextPath.length() + 1);
			}
			return templateFactory.create(templatePath, getClientInfo());
		}
	}
}
