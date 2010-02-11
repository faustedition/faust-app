package de.faustedition.model.xmldb;

import java.net.URI;
import java.net.URISyntaxException;

import javax.annotation.PostConstruct;
import javax.xml.transform.dom.DOMSource;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.UsernamePasswordCredentials;
import org.apache.commons.httpclient.auth.AuthScope;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.client.CommonsClientHttpRequestFactory;
import org.springframework.stereotype.Service;
import org.springframework.util.Assert;
import org.springframework.web.client.RestTemplate;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

@Service
public class XmlDbManager {
	private static Logger LOG = LoggerFactory.getLogger(XmlDbManager.class);
	public static final String EXIST_NS_URI = "http://exist.sourceforge.net/NS/exist";

	@Value("#{config['xmldb.base']}")
	private String dbBase;

	@Value("#{config['xmldb.user']}")
	private String dbUser;

	@Value("#{config['xmldb.password']}")
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

	public Node get(URI uri) {
		uri = relativize(uri);
		LOG.debug("Getting XML resource from XML-DB: {}", uri.toString());
		return rt.getForObject(uri, DOMSource.class).getNode();
	}

	public void put(URI uri, Document document) {
		uri = relativize(uri);
		LOG.debug("Putting XML document to XML-DB: {}", uri.toString());
		rt.put(uri, new DOMSource(document));
	}

	public Document resources() {
		return (Document) get(URI.create("Query/Resources.xq"));
	}

	public Document facsimileReferences() {
		return (Document) get(URI.create("Query/FacsimileRefs.xq"));
	}

	public Document encodingStati() {
		return (Document) get(URI.create("Query/EncodingStati.xq"));
	}

	protected URI relativize(URI uri) {
		Assert.isTrue(!uri.isAbsolute() && (uri.getPath() == null || !uri.getPath().startsWith("/")), "Invalid URI");
		return dbBaseUri.resolve(uri);
	}

}
