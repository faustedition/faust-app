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

package de.faustedition.document;

import de.faustedition.template.TemplateFinder;
import org.restlet.routing.Router;
import org.restlet.routing.Template;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class DocumentRouter extends Router implements InitializingBean {

	@Autowired
	private TemplateFinder templateFinder;

	@Autowired
	private DocumentImageLinkFinder imageLinkFinder;

	@Autowired
	private DocumentFinder documentFinder;

	@Override
	public void afterPropertiesSet() throws Exception {
		attach("styles", templateFinder);
		attach("imagelink", imageLinkFinder, Template.MODE_STARTS_WITH);
		attach(documentFinder, Template.MODE_STARTS_WITH);
	}
}
