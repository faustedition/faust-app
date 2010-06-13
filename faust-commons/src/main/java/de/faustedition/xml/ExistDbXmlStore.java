package de.faustedition.xml;

import static de.faustedition.xml.XPathUtil.xpath;
import static de.faustedition.xml.XmlDocument.xpath;

import java.net.URI;
import java.util.Iterator;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.Log;

public class ExistDbXmlStore extends BaseXmlStore {
	private static final String EXIST_NS_URI = "http://exist.sourceforge.net/NS/exist";
	private static SimpleNamespaceContext EXIST_NS_CONTEXT = new SimpleNamespaceContext();

	static {
		EXIST_NS_CONTEXT.bindNamespaceUri("exist", EXIST_NS_URI);
	}

	@Value("#{config['xmldb.user']}")
	private String dbUser;

	@Value("#{config['xmldb.password']}")
	private String dbPassword;

	private RestTemplate rt;

	public void afterPropertiesSet() throws Exception {
		super.afterPropertiesSet();

		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setDefaultMaxConnectionsPerHost(10);
		HttpClient httpClient = new HttpClient(connectionManager);
		httpClient.getParams().setAuthenticationPreemptive(true);
		httpClient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(dbUser, dbPassword));

		this.rt = new RestTemplate(new CommonsClientHttpRequestFactory(httpClient));
	}

	public Document get(URI uri) {
		uri = relativize(uri);
		Log.LOGGER.debug("Getting XML resource from XML-DB: {}", uri.toString());
		return (Document) rt.getForObject(uri, DOMSource.class).getNode();
	}

	public void put(URI uri, Document document) {
		uri = relativize(uri);
		Log.LOGGER.debug("Putting XML document to XML-DB: {}", uri.toString());
		rt.put(uri, new DOMSource(document));
	}

	public void delete(URI uri) {
		uri = relativize(uri);
		Log.LOGGER.debug("Deleting URI in XML-DB: {}", uri.toString());
		rt.delete(uri);
	}

	public SortedSet<URI> list(URI uri) {
		SortedSet<URI> contents = new TreeSet<URI>();
		if (!isCollection(uri)) {
			return contents;
		}

		XPathExpression contentXP = xpath("//exist:result/exist:collection/*", EXIST_NS_CONTEXT);
		for (Element content : new NodeListIterable<Element>(contentXP, get(uri))) {
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
	}

	@Override
	public Iterator<URI> iterator() {
		SortedSet<URI> contents = new TreeSet<URI>();
		for (Element resource : new NodeListIterable<Element>(xpath("//f:resource"), get(URI.create("Query/Resources.xq")))) {
			contents.add(URI.create(resource.getTextContent()));
		}
		return contents.iterator();
	}

	public Document facsimileReferences() {
		return get(URI.create("Query/FacsimileRefs.xq"));
	}

	public Document encodingStati() {
		return get(URI.create("Query/EncodingStati.xq"));
	}

	public Document identifiers() {
		return get(URI.create("Query/Identifiers.xq"));
	}
}
