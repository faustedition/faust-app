package de.faustedition.xml;

import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;



import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.JsonRepresentationFactory;
import de.faustedition.search.SearchResource;
import de.faustedition.template.TemplateRepresentationFactory;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class XMLQueryResource extends ServerResource {

	private static final Logger LOG = LoggerFactory.getLogger(SearchResource.class);

	@Autowired
	private XMLStorage xmlStorage;

	@Autowired
	private JsonRepresentationFactory jsonFactory;

	@Autowired
	private TemplateRepresentationFactory viewFactory;

	private String queryExpression;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		queryExpression = getQuery().getFirstValue("xpath");
	}


	@Get("html")
	public Representation overview() throws IOException {
		Map<String, Object> viewModel = new HashMap<String, Object>();
		viewModel.put("results", query());
		Map<String, String> params = this.getRequest().getResourceRef().getQueryAsForm().getValuesMap();
		return viewFactory.create("xml-query", getRequest().getClientInfo(), viewModel);
	}


	@Get("json")
	public Representation results() {

		final List<Map<String, Object>> results = query();
		return jsonFactory.map(results, false);
	}


	private List<Map<String, Object>> query()  {

		if (LOG.isInfoEnabled()) {
			LOG.info("XPath query for '{}'", queryExpression);
		}
		XPathExpression xpath = XPathUtil.xpath(queryExpression);
		final List<Map<String, Object>> results = Lists.newArrayList();

		for (FaustURI uri : xmlStorage.iterate(new FaustURI(FaustAuthority.XML, "/transcript/"))) {
			Map<String, Object> entry = Maps.<String, Object>newHashMap();
			entry.put("url", uri.toString());
			org.w3c.dom.Document document;
			try {
				document = XMLUtil.parse(xmlStorage.getInputSource(uri));
				NodeList xpathResultNodes = (NodeList) xpath.evaluate(document, XPathConstants.NODESET);
				List<Object> xpathResults = new ArrayList<Object>(xpathResultNodes.getLength());
				for (int i=0; i < xpathResultNodes.getLength(); i++) {
					try {
						Node node = xpathResultNodes.item(i);
						StringWriter writer = new StringWriter();
						Transformer transformer;
						transformer = TransformerFactory.newInstance().newTransformer();
						transformer.transform(new DOMSource(node), new StreamResult(writer));
						String xml = writer.toString();
						xpathResults.add(xml);
					} catch (TransformerConfigurationException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerFactoryConfigurationError e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					} catch (TransformerException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				if (!xpathResults.isEmpty()) {
					entry.put("results", xpathResults);
					results.add(entry);
				}
			} catch (SAXException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (XPathExpressionException e) {

			}
		}
		return results;
	}
}
