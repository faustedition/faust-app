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

package de.faustedition.xml;

import de.faustedition.FaustURI;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.data.MediaType;
import org.restlet.ext.xml.XmlRepresentation;
import org.restlet.resource.Finder;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.xml.sax.InputSource;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.Writer;
import java.util.Deque;

@Component
public class XMLFinder extends Finder {

	@Autowired
	private XMLStorage xml;

	@Autowired
	private Logger logger;

	@Override
	public ServerResource find(Request request, Response response) {
		final Deque<String> path = FaustURI.toPathDeque(request.getResourceRef().getRelativeRef().getPath());
		logger.debug("Finding XML resource for " + path);
		
		try {
			final FaustURI uri = xml.walk(path);
			if (uri == null) {
				return null;
			}
			logger.debug("Delivering XML for " + uri);
			return new XMLResource(uri);
		} catch (IllegalArgumentException e) {
			logger.debug("Parse error while resolving XML resource for " + path, e);
			return null;
		}
	}

	protected class XMLResource extends ServerResource {
		protected final FaustURI uri;

		protected XMLResource(FaustURI uri) {
			this.uri = uri;
		}

		@Get("xml")
		public XmlRepresentation render() {
			return new XmlRepresentation(MediaType.APPLICATION_XML) {

				@Override
				public void write(Writer writer) throws IOException {
					try {
						final Transformer transformer = XMLUtil.transformerFactory().newTransformer();
						transformer.transform(new SAXSource(getInputSource()), new StreamResult(writer));
					} catch (TransformerException e) {
						throw new IOException("XML error while streaming '" + uri + "'", e);
					}

				}

				@Override
				public InputSource getInputSource() throws IOException {
					return xml.getInputSource(uri);
				}
			};
		}
	}
}
