package de.faustedition.genesis;

import static de.faustedition.xml.XPathUtil.xpath;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;

import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.restlet.routing.Router;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.Singleton;

import de.faustedition.document.Archive;
import de.faustedition.document.Document;
import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.graph.FaustGraph;
import de.faustedition.inject.InjectorFinder;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.xml.NodeListWrapper;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

@Singleton
public class GeneticGraphRouter extends Router {

	@Inject
	public GeneticGraphRouter(Injector injector) {
		InjectorFinder geneticGraphResource = new InjectorFinder(injector, GeneticGraphResource.class);
		attach("", geneticGraphResource);
		attach("{filter}", geneticGraphResource);
	}

	@GraphDatabaseTransactional
	public static class GeneticGraphResource extends ServerResource {

		private final XMLStorage xmlStorage;
		private final TemplateRepresentationFactory viewFactory;
		private final FaustGraph graph;

		@Inject
		public GeneticGraphResource(XMLStorage xmlStorage, FaustGraph graph, TemplateRepresentationFactory viewFactory) {
			this.xmlStorage = xmlStorage;
			this.graph = graph;
			this.viewFactory = viewFactory;
		}

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
