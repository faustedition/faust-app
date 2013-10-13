package de.faustedition.transcript;

import de.faustedition.Templates;
import de.faustedition.db.tables.records.TranscriptRecord;
import de.faustedition.document.Archive;
import de.faustedition.document.MaterialUnit;
import de.faustedition.graph.Graph;
import org.neo4j.graphdb.NotFoundException;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import java.io.IOException;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/transcript")
public class TranscriptResource {

    private final Graph graph;
    private final Transcripts transcripts;
    private final Templates templates;

    @Inject
    public TranscriptResource(Graph graph,
                              Transcripts transcripts,
                              Templates templates) {
        this.graph = graph;
        this.transcripts = transcripts;
        this.templates = templates;
    }

    @GET
    @Path("/{id}")
    public Response page(@PathParam("id") final long id, @Context final Request request) throws Exception {
        return graph.transaction(new Graph.TransactionCallback<Response>() {
            @Override
            public Response doInTransaction(Graph graph) throws Exception {
                final MaterialUnit materialUnit = materialUnit(graph, id);
                if (materialUnit.getType() != MaterialUnit.Type.ARCHIVALDOCUMENT) {
                    // FIXME when requesting a transcript of a non-document, we loop endlessly in the graph in getArchive()!
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Only text transcripts can be displayed!").build());
                }

                final Archive archive = materialUnit.getArchive();
                return templates.render(request, new Templates.ViewAndModel("transcript")
                        .add("id", materialUnit.node.getId())
                        .add("archiveName", (archive == null ? null : archive.getName()))
                        .add("archiveId", (archive == null ? null : archive.getId()))
                        .add("waId", materialUnit.getMetadataValue("wa-id"))
                        .add("callnumber", materialUnit.getMetadataValue("callnumber"))
                );
            }
        });
    }

    @GET
    @Path("/source/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Source xml(@PathParam("id") final long id) {
        /*
        try {
            return Graph.execute(db, new Graph.Transaction<Source>() {
                @Override
                public Source execute(Graph graph) throws Exception {
                    final Layer<JsonNode> transcript = transcript(graph, id);
                    for (Anchor<JsonNode> anchor : transcript.getAnchors()) {
                        final Layer<JsonNode> text = anchor.getText();
                        if (TextConstants.XML_SOURCE_NAME.equals(text.getName())) {
                            return new SAXSource(new InputSource(new StringReader(text.read())));
                        }
                    }
                    throw notFound(id);
                }
            });
        } catch (Exception e) {
            throw WebApplication.propagateExceptions(e);
        }
        */
        return null;
    }

    @GET
    @Path("/source/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String plainText(@PathParam("id") final long id) {
        /*
        try {
            return Graph.execute(db, new Graph.Transaction<String>() {
                @Override
                public String execute(Graph graph) throws Exception {
                    return transcript(graph, id).read();
                }
            });
        } catch (Exception e) {
            throw WebApplication.propagateExceptions(e);
        }
        */
        return null;
    }

    @GET
    @Path("/source/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> json(@PathParam("id") final long id) {
        /*
        try {
            return Graph.execute(db, new Graph.Transaction<Map<String, Object>>() {
                @Override
                public Map<String, Object> execute(Graph graph) throws Exception {
                    final Map<String, Name> names = Maps.newHashMap();
                    final Layer<JsonNode> transcript = transcript(graph, id);
                    final ArrayList<Layer<JsonNode>> annotations = Lists.newArrayList();
                    for (Layer<JsonNode> annotation : textRepository.query(text(transcript))) {
                        final Name name = annotation.getName();
                        names.put(Long.toString(name.hashCode()), name);
                        annotations.add(annotation);
                    }

                    final Map<String ,Object> model = Maps.newHashMap();
                    model.put("text", transcript);
                    model.put("textContent", transcript.read());
                    model.put("names", names);
                    model.put("annotations", annotations);
                    return model;
                }
            });
        } catch (Exception e) {
            throw WebApplication.propagateExceptions(e);
        }
        */
        return null;
    }

    protected MaterialUnit materialUnit(Graph graph, long materialUnitId) {
        try {
            return MaterialUnit.forNode(graph.db().getNodeById(materialUnitId));
        } catch (NotFoundException e) {
            throw notFound(materialUnitId);
        }
    }

    protected TranscriptRecord transcript(Graph graph, long materialUnitId) throws IOException, XMLStreamException {
        return null;
    }

    protected WebApplicationException notFound(long materialUnitId) {
        return new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(materialUnitId).build());
    }
}
