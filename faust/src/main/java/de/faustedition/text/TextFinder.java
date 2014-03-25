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

package de.faustedition.text;

import java.util.Deque;

import org.restlet.Request;
import org.restlet.Response;
import org.restlet.resource.Finder;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import de.faustedition.FaustURI;
import de.faustedition.xml.XMLStorage;

@Component
public class TextFinder extends Finder {

	@Autowired
	private XMLStorage xml;

	@Autowired
	private Logger logger;

	@Autowired
	private TextManager textManager;

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public ServerResource find(Request request, Response response) {
		final Deque<String> path = FaustURI.toPathDeque(request.getResourceRef().getRelativeRef().getPath());
		path.addFirst("text");

		logger.debug("Finding text resource for " + path);

		try {
			final FaustURI uri = xml.walk(path);
			if (uri == null) {
				return null;
			}

			logger.debug("Finding text for " + uri);
			final Text text = textManager.find(uri);
			if (text == null) {
				return null;
			}

			final TextResource resource = applicationContext.getBean(TextResource.class);
			resource.setText(text);
			return resource;
		} catch (IllegalArgumentException e) {
			logger.debug("Parse error while resolving text resource for " + path, e);
			return null;
		}
	}
}
