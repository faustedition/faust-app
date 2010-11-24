package de.faustedition.xml;

import java.io.IOException;
import java.io.Writer;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;

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
import org.xml.sax.InputSource;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;

@Singleton
public class XMLFinder extends Finder {

	private final XMLStorage xml;
	private final Logger logger;

	@Inject
	public XMLFinder(XMLStorage xml, Logger logger) {
		this.xml = xml;
		this.logger = logger;
	}

	@Override
	public ServerResource find(Request request, Response response) {
		final String path = request.getResourceRef().getRelativeRef().getPath().replaceAll("^/+", "").replaceAll("/+$", "");

		logger.fine("Finding XML resource for '" + path + "'");
		final ArrayDeque<String> pathDeque = new ArrayDeque<String>(Arrays.asList(path.split("/+")));
		if (pathDeque.size() == 0) {
			return null;
		}

		FaustURI uri = new FaustURI(FaustAuthority.XML, "/");
		try {
			while (pathDeque.size() > 0) {
				FaustURI next = uri.resolve(pathDeque.pop());
				if (xml.isDirectory(next)) {
					uri = FaustURI.parse(next.toString() + "/");
					continue;
				}
				if (xml.isResource(next)) {
					uri = next;
					break;
				}
				return null;
			}
		} catch (IllegalArgumentException e) {
			logger.log(Level.FINE, "Parse error while resolving XML resource for '" + path + "'", e);
			return null;
		}

		logger.fine("Finding XML for " + uri);
		return (xml.isResource(uri) ? new XMLResource(uri) : null);
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
