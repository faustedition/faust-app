package de.faustedition.xml;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;

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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

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
	private String folder;

	private enum Mode {
		XML, VALUES, FILES
	}

	private Mode mode;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		queryExpression = getQuery().getFirstValue("xpath");
		folder = getQuery().getFirstValue("folder") != null ? getQuery().getFirstValue("folder") : "transcript";
		if (folder != null && !xmlStorage.isDirectory(new FaustURI(FaustAuthority.XML, "/" + folder + "/")))
			throw new IllegalArgumentException("Invalid folder: " + folder);
		mode = getQuery().getFirstValue("mode") != null ?
				Mode.valueOf(getQuery().getFirstValue("mode").toUpperCase()) :
					Mode.XML;
	}


	@Get("html")
	public Representation queryForm() throws IOException {
		Map<String, Object> viewModel = new HashMap<String, Object>();
		List<Map<String, Object>> files = queryExpression != null ? 
				query() : Lists.<Map<String,Object>>newArrayList();
				viewModel.put("folder", folder);
				viewModel.put("xpath", queryExpression);
				if (mode == Mode.XML || mode == Mode.FILES) {
					viewModel.put("files", files);
				} else if (mode == Mode.VALUES) {
					Set<String> uniqueValues = Sets.newTreeSet();
					for (Map<String,Object> file : files) {
						if (file.containsKey("results")) {
							uniqueValues.addAll((List<String>)file.get("results"));
						}
					}
					viewModel.put("values", uniqueValues.toArray());
				}
				if (mode != null)
					viewModel.put("mode", mode.toString().toLowerCase());
				return viewFactory.create("xml-query", getRequest().getClientInfo(), viewModel);
	}


	@Get("json")
	public Representation queryResults() {

		final List<Map<String, Object>> results = query();
		return jsonFactory.map(results, false);
	}


	private List<Map<String, Object>> query()  {

		if (LOG.isInfoEnabled()) {
			LOG.info("XPath query for '{}'", queryExpression);
		}
		
		XPathExpression xpath = XPathUtil.xpath(queryExpression);
		final List<Map<String, Object>> files = Lists.newArrayList();

		for (FaustURI uri : xmlStorage.iterate(new FaustURI(FaustAuthority.XML, "/" + folder + "/"))) {
			Map<String, Object> entry = Maps.<String, Object>newHashMap();
			entry.put("uri", uri.toString());
			uri.resolve("");
			org.w3c.dom.Document document;
			try {
				document = XMLUtil.parse(xmlStorage.getInputSource(uri));
				NodeList xpathResultNodes = (NodeList) xpath.evaluate(document, XPathConstants.NODESET);
				List<String> xpathResults = new ArrayList<String>(xpathResultNodes.getLength());
				for (int i=0; i < xpathResultNodes.getLength(); i++) {
					Node node = xpathResultNodes.item(i);
					xpathResults.add(XMLUtil.toString(node));

				}
				if (!xpathResults.isEmpty()) {
					entry.put("results", xpathResults);
				}
			} catch (Exception e) {
				entry.put("error", e.toString());
			} finally {
				if (entry.containsKey("results") || entry.containsKey("error"))
					files.add(entry);
			}
		}
		return files;
	}
}
