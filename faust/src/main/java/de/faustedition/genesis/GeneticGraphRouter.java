package de.faustedition.genesis;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpressionException;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.SAXException;

import com.google.common.collect.Iterables;

import de.faustedition.ApplicationContextFinder;
import de.faustedition.document.Archive;
import de.faustedition.document.Document;
import de.faustedition.graph.FaustGraph;
import de.faustedition.template.TemplateRepresentationFactory;

@Component
public class GeneticGraphRouter extends Router implements InitializingBean {

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public void afterPropertiesSet() throws Exception {
		ApplicationContextFinder<GeneticGraphResource> geneticGraphResource = new ApplicationContextFinder<GeneticGraphResource>(applicationContext, GeneticGraphResource.class);
		attach("", geneticGraphResource);
		attach("{filter}", geneticGraphResource);
	}

	@Component
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public static class GeneticGraphResource extends ServerResource {

		@Autowired
		private TemplateRepresentationFactory viewFactory;

		@Autowired
		private FaustGraph graph;

		@Get
		public Representation render() throws IOException, XPathExpressionException, SAXException {
			final Map<String, Object> model = new HashMap<String, Object>();
			final String filter = (String) getRequestAttributes().get("filter");

			if (filter == null) {


				final SortedSet<Document> archivalUnits = new TreeSet<Document>(new Comparator<Document>() {
					@Override
					public int compare(Document o1, Document o2) {
						final String o1cn = o1.getMetadataValue("callnumber");
						final String o2cn = o2.getMetadataValue("callnumber");
						return (o1cn == null || o2cn == null) ? 0 : o1cn.compareTo(o2cn);
					}
				});


				for (Archive archive : graph.getArchives()) {
					for (Document document : Iterables.filter(archive, Document.class)) {
						//temporallyPrecedes = document.geneticallyRelatedTo(MacrogeneticRelationManager.TEMP_PRE_REL);
						archivalUnits.add(document);
					}
						

					//Iterables.addAll(archivalUnits, Iterables.filter(archive, Document.class));
				}
				model.put("archivalUnits", archivalUnits);
				return viewFactory.create("genesis/graph", getRequest().getClientInfo(), model);
			}

			getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, filter);
			return null;

		}
	}
}
