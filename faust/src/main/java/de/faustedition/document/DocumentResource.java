package de.faustedition.document;

import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.WebApplication;
import de.faustedition.graph.Graph;
import de.faustedition.template.Templates;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ArrayNode;
import org.codehaus.jackson.node.ObjectNode;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

@Path("/document")
@Singleton
public class DocumentResource {

    private final GraphDatabaseService graphDatabaseService;
    private final ObjectMapper objectMapper;
    private final Templates templates;

    @Inject
    public DocumentResource(GraphDatabaseService graphDatabaseService, ObjectMapper objectMapper, Templates templates) {
        this.graphDatabaseService = graphDatabaseService;
        this.objectMapper = objectMapper;
        this.templates = templates;
    }

    @Path("/imagelink/{path: .+?}")
    @GET
    @Produces(MediaType.TEXT_HTML)
    public Response imageLinks(@PathParam("path") final String path, @Context final Request request, @Context final SecurityContext sc) {
        return Graph.execute(graphDatabaseService, new Graph.Transaction<Response>() {
            @Override
            public Response execute(Graph graph) throws Exception {
                final Deque<String> pathDeque = WebApplication.pathDeque(path);

                final int page = Integer.parseInt(Objects.firstNonNull(pathDeque.pollLast(), "1"));
                final Document document = document(graph, toSource(pathDeque));

                final Map<String, Object> viewModel = new HashMap<String, Object>();
                viewModel.put("pageNum", page);
                viewModel.put("document", document);
                viewModel.put("facsimileUrl", facsimileUrl(document, page));

                return templates.render("document/imagelink", viewModel, request, sc);
            }
        });
    }

    @Path("/styles")
    @GET
    public Response styles(@Context final Request request, @Context final SecurityContext sc) {
        return templates.render("document/styles", request, sc);
    }

    @Path("{path: .+?}")
	@GET
    @Produces(MediaType.TEXT_HTML)
	public Response overview(@PathParam("path") final String path, @Context final Request request, @Context final SecurityContext sc) {
        try {
            return Graph.execute(graphDatabaseService, new Graph.Transaction<Response>() {
                @Override
                public Response execute(Graph graph) throws Exception {
                    final FaustURI uri = toSource(path);
                    final Document document = document(graph, uri);
                    final Map<String, Object> viewModel = new HashMap<String, Object>();
                    viewModel.put("document", document);
                    viewModel.put("contents", document.getSortedContents());
                    viewModel.put("path", uri);

                    return templates.render("document/document-app", viewModel, request, sc);
                }
            });
        } catch (Throwable t) {
            throw WebApplication.propagateExceptions(t);
        }
    }

    @Path("{path: .+?}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
	public JsonNode documentStructure(@PathParam("path") final String path) {
        try {
            return Graph.execute(graphDatabaseService, new Graph.Transaction<JsonNode>() {

                @Override
                public JsonNode execute(Graph graph) throws Exception {
                    return toJson(document(graph, toSource(path)));
                }

                protected JsonNode toJson(MaterialUnit unit) throws IOException {
                    final ObjectNode unitNode = objectMapper.createObjectNode();
                    unitNode.put("type", unit.getType().name().toLowerCase());
                    unitNode.put("order", unit.getOrder());
                    unitNode.put("id", unit.node.getId());

                    if (unit.getTranscriptSource() != null) {
                        final ObjectNode transcriptNode = objectMapper.createObjectNode();
                        transcriptNode.put("source", unit.getTranscriptSource().toString());

                        final FaustURI facsimile = unit.getFacsimile();
                        if (facsimile != null) {
                            transcriptNode.put("facsimile", facsimile.toString());
                        }

                        unitNode.put("transcript", transcriptNode);
                    }

                    final ArrayNode contentsNode = objectMapper.createArrayNode();
                    for (MaterialUnit content : new TreeSet<MaterialUnit>(unit)) {
                        contentsNode.add(toJson(content));
                    }
                    unitNode.put("contents", contentsNode);

                    return unitNode;
                }
            });
        } catch (Throwable t) {
            throw WebApplication.propagateExceptions(t);
        }
    }

    protected FaustURI toSource(Deque<String> path){
        final Deque<String> pathDeque = new ArrayDeque<String>(path);
        pathDeque.addFirst("document");
        return new FaustURI(FaustAuthority.XML, pathDeque);
    }

    protected FaustURI toSource(String path) {
        return toSource(WebApplication.pathDeque(path));
    }

    protected Document document(Graph graph, FaustURI uri) throws WebApplicationException {
        final Document document = Document.findBySource(graph.db(), uri);
        if (document == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(uri.toString()).build());
        }
        return document;
    }

    protected String facsimileUrl(Document document, int page) {
        try {
            return URLEncoder.encode("&SDS=0,90&CNT=1.0&WID=800&QLT=90&CVT=jpeg", "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw Throwables.propagate(e);
        }
    }
}
