package de.faustedition.xml;

import static de.faustedition.xml.XPathUtil.xpath;
import static de.faustedition.xml.XmlDocument.xpath;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.annotation.PostConstruct;
import javax.xml.transform.dom.DOMSource;
import javax.xml.xpath.XPathExpression;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.util.xml.SimpleNamespaceContext;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Service
public class XmlDbManager {
	private static Logger LOG = LoggerFactory.getLogger(XmlDbManager.class);
	public static final String EXIST_NS_URI = "http://exist.sourceforge.net/NS/exist";
	private static SimpleNamespaceContext EXIST_NS_CONTEXT = new SimpleNamespaceContext();

	static {
		EXIST_NS_CONTEXT.bindNamespaceUri("exist", EXIST_NS_URI);
	}

	@Value("#{xmlConfig['xmldb.base']}")
	private String dbBase;

	@Value("#{xmlConfig['xmldb.user']}")
	private String dbUser;

	@Value("#{xmlConfig['xmldb.password']}")
	private String dbPassword;

	private URI dbBaseUri;

	private RestTemplate rt;

	@PostConstruct
	public void init() throws URISyntaxException {
		this.dbBaseUri = new URI(dbBase);

		MultiThreadedHttpConnectionManager connectionManager = new MultiThreadedHttpConnectionManager();
		connectionManager.getParams().setDefaultMaxConnectionsPerHost(10);
		HttpClient httpClient = new HttpClient(connectionManager);
		httpClient.getParams().setAuthenticationPreemptive(true);
		httpClient.getState().setCredentials(AuthScope.ANY, new UsernamePasswordCredentials(dbUser, dbPassword));

		this.rt = new RestTemplate(new CommonsClientHttpRequestFactory(httpClient));
	}

	public XmlDbQueryResult query(URI uri, XmlDbQuery query) {
		uri = relativize(uri);
		LOG.debug("Posting XQuery to XML-DB: {} ==> {}", uri.toString(), query.getXquery());
		return XmlDbQueryResult.parse((Document) rt.postForObject(uri, new DOMSource(query.toDocument()), DOMSource.class)
				.getNode());
	}

	public XmlDbQueryResult query(XmlDbQuery query) {
		return query(URI.create(""), query);
	}

	public Document get(URI uri) {
		uri = relativize(uri);
		LOG.debug("Getting XML resource from XML-DB: {}", uri.toString());
		return (Document) rt.getForObject(uri, DOMSource.class).getNode();
	}

	public void put(URI uri, Document document) {
		uri = relativize(uri);
		LOG.debug("Putting XML document to XML-DB: {}", uri.toString());
		rt.put(uri, new DOMSource(document));
	}

	public void delete(URI uri) {
		uri = relativize(uri);
		LOG.debug("Deleting URI in XML-DB: {}", uri.toString());
		rt.delete(uri);
	}

	public SortedSet<URI> contentsOf(URI uri) {
		Assert.isTrue(isCollectionURI(uri), "URI does not point to a collection");
		SortedSet<URI> contents = new TreeSet<URI>();
		XPathExpression contentXPath = xpath("//exist:result/exist:collection/*", EXIST_NS_CONTEXT);
		Document contentDocument = get(uri);

		for (Element content : new NodeListIterable<Element>(contentXPath, contentDocument)) {
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

	public static boolean isCollectionURI(URI uri) {
		return StringUtils.isBlank(uri.getPath()) || uri.getPath().endsWith("/");
	}

	public Document resources() {
		return (Document) get(URI.create("Query/Resources.xq"));
	}

	public SortedSet<URI> resourceUris() {
		SortedSet<URI> resourceUris = new TreeSet<URI>();
		for (Element resource : new NodeListIterable<Element>(xpath("//f:resource"), resources())) {
			resourceUris.add(URI.create(resource.getTextContent()));
		}
		return resourceUris;
	}

	public Document facsimileReferences() {
		return (Document) get(URI.create("Query/FacsimileRefs.xq"));
	}

	public Document encodingStati() {
		return (Document) get(URI.create("Query/EncodingStati.xq"));
	}

	protected URI relativize(URI uri) {
		Assert.isTrue(!uri.isAbsolute() && (StringUtils.isBlank(uri.getPath()) || !uri.getPath().startsWith("/")),
				"Invalid URI");
		return dbBaseUri.resolve(uri);
	}

}
