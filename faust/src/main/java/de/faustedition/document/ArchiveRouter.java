package de.faustedition.document;

import static de.faustedition.xml.XPathUtil.xpath;

import java.io.IOException;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import javax.xml.xpath.XPathExpression;

import org.neo4j.graphdb.GraphDatabaseService;
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
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.base.Throwables;
import com.google.common.collect.Iterables;

import de.faustedition.ApplicationContextFinder;
import de.faustedition.graph.Graph;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.xml.NodeListWrapper;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

@Component
public class ArchiveRouter extends Router implements InitializingBean {

	@Autowired
	private ApplicationContext applicationContext;

	@Override
	public void afterPropertiesSet() throws Exception {
		ApplicationContextFinder<ArchiveResource> archiveResource = new ApplicationContextFinder<ArchiveResource>(applicationContext, ArchiveResource.class);
		attach("", archiveResource);
		attach("{id}", archiveResource);
	}

	@Component
	@Scope(BeanDefinition.SCOPE_PROTOTYPE)
	public static class ArchiveResource extends ServerResource {

		@Autowired
		private XMLStorage xmlStorage;

		@Autowired
		private TemplateRepresentationFactory viewFactory;

		@Autowired
		private GraphDatabaseService graphDatabaseService;

		@Get
		public Representation render() {
            return Graph.execute(graphDatabaseService, new Graph.Transaction<Representation>() {
                @Override
                public Representation execute(Graph graph) throws Exception {
                    final Map<String, Object> model = new HashMap<String, Object>();
                    final String id = (String) getRequestAttributes().get("id");

                    final org.w3c.dom.Document archives = XMLUtil.parse(xmlStorage.getInputSource(ArchiveInitializer.ARCHIVE_DESCRIPTOR_URI));
                    if (id == null) {
                        model.put("archives", archives.getDocumentElement());
                        return viewFactory.create("document/archives", getRequest().getClientInfo(), model);
                    }

                    final Archive archive = graph.getArchives().findById(id);
                    if (archive == null) {
                        getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, id);
                        return null;
                    }

                    final SortedSet<Document> archivalUnits = new TreeSet<Document>(new Comparator<Document>() {
                        @Override
                        public int compare(Document o1, Document o2) {
                            final String o1cn = o1.toString();
                            final String o2cn = o2.toString();
                            return (o1cn == null || o2cn == null) ? 0 : o1cn.compareTo(o2cn);
                        }
                    });
                    Iterables.addAll(archivalUnits, Iterables.filter(archive, Document.class));
                    model.put("archivalUnits", archivalUnits);

                    final XPathExpression xpathById = xpath("/f:archives/f:archive[@id='" + id + "']");
                    final Element archiveData = new NodeListWrapper<Element>(xpathById, archives).singleResult(Element.class);
                    if (archiveData == null) {
                        getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, id);
                        return null;
                    }
                    model.put("archive", archiveData);

                    return viewFactory.create("document/archive", getRequest().getClientInfo(), model);
                }
            });
		}
	}
}
