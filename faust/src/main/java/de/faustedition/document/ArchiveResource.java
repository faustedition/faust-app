package de.faustedition.document;

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
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import com.google.common.collect.Iterables;
import com.google.inject.Inject;

import de.faustedition.graph.GraphDatabaseTransactional;
import de.faustedition.graph.GraphReference;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.xml.NodeListWrapper;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

@GraphDatabaseTransactional
public class ArchiveResource extends ServerResource {

    private final XMLStorage xmlStorage;
    private final TemplateRepresentationFactory viewFactory;
    private final GraphReference graph;

    @Inject
    public ArchiveResource(XMLStorage xmlStorage, GraphReference graph, TemplateRepresentationFactory viewFactory) {
        this.xmlStorage = xmlStorage;
        this.graph = graph;
        this.viewFactory = viewFactory;
    }

    @Get
    public Representation render() throws IOException, XPathExpressionException, SAXException {
        final Map<String, Object> model = new HashMap<String, Object>();
        final String id = (String) getRequestAttributes().get("id");
        final org.w3c.dom.Document archives = XMLUtil.parse(xmlStorage.getInputSource(ArchiveManager.ARCHIVE_DESCRIPTOR_URI));

        if (id == null) {
            model.put("archives", archives.getDocumentElement());
            return viewFactory.create("document/archives", getRequest().getClientInfo(), model);
        }

        Archive archive = graph.getArchives().findById(id);
        if (archive == null) {
            getResponse().setStatus(Status.CLIENT_ERROR_NOT_FOUND, id);
            return null;
        }

        final SortedSet<Document> archivalUnits = new TreeSet<Document>(new Comparator<Document>() {
            @Override
            public int compare(Document o1, Document o2) {
                final String o1cn = o1.getMetadataValue("callnumber");
                final String o2cn = o2.getMetadataValue("callnumber");
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
}
