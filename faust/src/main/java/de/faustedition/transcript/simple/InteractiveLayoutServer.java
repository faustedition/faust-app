/*
 * Copyright (c) 2015 Faust Edition development team.
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

package de.faustedition.transcript.simple;

import org.restlet.*;
import org.restlet.data.MediaType;
import org.restlet.data.Protocol;
import org.restlet.representation.FileRepresentation;
import org.restlet.representation.StringRepresentation;
import org.restlet.representation.WriterRepresentation;
import org.restlet.resource.Directory;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.restlet.routing.Template;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;

/**
 * User: moz
 * Date: 28/04/15
 * Time: 1:27 PM
 */
public class InteractiveLayoutServer extends ServerResource {

	protected static int port;
	protected static String staticPath;
	protected static FileRepresentation htmlPage;

	public static void main(String[] args) throws Exception {
		if (args.length != 2) {
			System.err.print("Usage: InteractiveLayoutServer static_dir tcp_port");
			System.exit(-1);
		} else {
			staticPath = args[0];
			port = Integer.parseInt(args[1]);
			start();

		}
	}

	private static void start() throws Exception {

		Component component = new Component();
		component.getServers().add(Protocol.HTTP, port);
		component.getClients().add(Protocol.FILE);
		Application application = new Application();
		component.getDefaultHost().attach(application);
		Router router = new Router();
		router.setDefaultMatchingMode(Template.MODE_STARTS_WITH);

		router.attach("/static/", new Directory(component.getContext().createChildContext(), "file://" + staticPath));
		router.attach("/", InteractiveLayoutServer.class);
		application.setInboundRoot(router);
		component.start();
	}

	@Get("html")
	public FileRepresentation index() {
		htmlPage = new FileRepresentation(
				staticPath + "/interactive-layout/interactive-layout.html",
				MediaType.TEXT_HTML);
		return htmlPage ;
	}

	@Post("xml")
	public StringRepresentation transform(InputStream in) throws TransformerException, IOException, XMLStreamException {
		StringWriter writer = new StringWriter();
		SimpleTransform.simpleTransform(in, writer);
		return new StringRepresentation(writer.toString(), MediaType.APPLICATION_JSON);
	}
}
