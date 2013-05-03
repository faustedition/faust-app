package de.faustedition.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.Deque;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;

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

import de.faustedition.FaustURI;

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
