package de.faustedition.document;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import de.faustedition.WebApplication;
import de.faustedition.graph.Graph;
import de.faustedition.template.Templates;
import de.faustedition.xml.NodeListWrapper;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import org.neo4j.graphdb.GraphDatabaseService;
import org.w3c.dom.Element;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import javax.xml.xpath.XPathExpression;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

import static de.faustedition.xml.XPathUtil.xpath;

@Path("/archive")
@Singleton
public class ArchiveResource {

    private final XMLStorage xmlStorage;
    private final GraphDatabaseService graphDatabaseService;
    private final Templates templates;

    @Inject
    public ArchiveResource(XMLStorage xmlStorage, GraphDatabaseService graphDatabaseService, Templates templates) {
        this.xmlStorage = xmlStorage;
        this.graphDatabaseService = graphDatabaseService;
        this.templates = templates;
    }

    @GET
    public Response index(@Context final Request request, @Context final SecurityContext sc) {
        return Graph.execute(graphDatabaseService, new Graph.Transaction<Response>() {
            @Override
            public Response execute(Graph graph) throws Exception {
                final org.w3c.dom.Document archives = XMLUtil.parse(xmlStorage.getInputSource(ArchiveInitializer.ARCHIVE_DESCRIPTOR_URI));

                final Map<String, Object> model = new HashMap<String, Object>();
                model.put("archives", archives.getDocumentElement());
                return templates.render("document/archives", model, request, sc);
            }
        });
    }

    @Path("/{id}")
    @GET
    public Response archive(@PathParam("id") final String id, @Context final Request request, @Context final SecurityContext sc) {
        try {
            return Graph.execute(graphDatabaseService, new Graph.Transaction<Response>() {
                @Override
                public Response execute(Graph graph) throws Exception {
                    final org.w3c.dom.Document archives = XMLUtil.parse(xmlStorage.getInputSource(ArchiveInitializer.ARCHIVE_DESCRIPTOR_URI));
                    final Archive archive = graph.getArchives().findById(id);
                    if (archive == null) {
                        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(id).build());
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

                    final XPathExpression xpathById = xpath("/f:archives/f:archive[@id='" + id + "']");
                    final Element archiveData = new NodeListWrapper<Element>(xpathById, archives).singleResult(Element.class);
                    if (archiveData == null) {
                        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(id).build());
                    }

                    final Map<String, Object> model = Maps.newHashMap();
                    model.put("archivalUnits", archivalUnits);
                    model.put("archive", archiveData);
                    return templates.render("document/archive", model, request, sc);
                }
            });
        } catch (Throwable t) {
            throw WebApplication.propagateExceptions(t);
        }
    }
}
