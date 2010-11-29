package de.faustedition.xml;

import static de.faustedition.xml.XPathUtil.xpath;

import java.io.IOException;
import java.net.URI;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.restlet.data.ChallengeScheme;
import org.restlet.ext.xml.DomRepresentation;
import org.restlet.resource.ClientResource;
import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.name.Named;

public class XMLDatabase implements Iterable<URI> {
	public static final String EXIST_NS_URI = "http://exist.sourceforge.net/NS/exist";

	private final URI base;
	private final String dbUser;
	private final String dbPassword;
	private final Logger logger;
	public static final String EXIST_NS_PREFIX = "exist";

	@Inject
	public XMLDatabase(@Named("xmldb.base") String baseUri, @Named("xmldb.user") String dbUser,
			@Named("xmldb.password") String dbPassword, Logger logger) {
		this.base = URI.create(baseUri);
		this.dbUser = dbUser;
		this.dbPassword = dbPassword;
		this.logger = logger;
	}

	public Document get(URI uri) throws IOException {
		logger.fine(String.format("Getting XML resource from XML-DB: %s", uri.toString()));
		DomRepresentation representation = createClientResource(uri).get(DomRepresentation.class);
		representation.setNamespaceAware(true);
		return representation.getDocument();
	}

	public void put(URI uri, Document document) {
		logger.fine(String.format("Putting XML document to XML-DB: %s", uri.toString()));
		createClientResource(uri).put(document);
	}

	public void delete(URI uri) {
		logger.fine(String.format("Deleting URI in XML-DB: %s", uri.toString()));
		createClientResource(uri).delete();
	}

	public SortedSet<URI> list(URI uri) throws IOException {
		try {
			SortedSet<URI> contents = new TreeSet<URI>();
			if (!isCollection(uri)) {
				return contents;
			}

			XPathExpression contentXP = xpath("//exist:result/exist:collection/*", CustomNamespaceContext.INSTANCE);
			for (Element content : new NodeListWrapper<Element>(contentXP, get(uri))) {
				if (!EXIST_NS_URI.equals(content.getNamespaceURI())) {
					continue;
				}

				String localName = content.getLocalName();
				if ("collection".equals(localName)) {
					contents.add(uri.resolve(content.getAttribute("name") + "/"));
				} else if ("resource".equals(localName)) {
					contents.add(uri.resolve(content.getAttribute("name")));
				}
			}
			return contents;
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		}
	}

	public Iterator<URI> iterator() {
		try {
			SortedSet<URI> contents = new TreeSet<URI>();
			for (Element resource : new NodeListWrapper<Element>(xpath("//f:resource"),
					get(URI.create("Query/Resources.xq")))) {
				contents.add(URI.create(resource.getTextContent()));
			}
			return contents.iterator();
		} catch (XPathExpressionException e) {
			throw new RuntimeException(e);
		} catch (DOMException e) {
			throw new RuntimeException(e);
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

	public Document facsimileReferences() throws IOException {
		return get(URI.create("Query/FacsimileRefs.xq"));
	}

	public Document encodingStati() throws IOException {
		return get(URI.create("Query/EncodingStati.xq"));
	}

	public Document identifiers() throws IOException {
		return get(URI.create("Query/Identifiers.xq"));
	}

	protected ClientResource createClientResource(URI uri) {
		ClientResource resource = new ClientResource(relativize(uri));
		resource.setChallengeResponse(ChallengeScheme.HTTP_BASIC, dbUser, dbPassword);
		return resource;
	}

	protected boolean isCollection(URI uri) {
		return (uri.getPath() == null || uri.getPath().length() == 0) || uri.getPath().endsWith("/");
	}

	protected URI relativize(URI uri) {
		Preconditions.checkArgument(!uri.isAbsolute()
				&& (uri.getPath() == null || uri.getPath().length() == 0 || !uri.getPath().startsWith("/")));
		return base.resolve(uri);
	}
}
