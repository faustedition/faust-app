package de.faustedition.transcript;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.faustedition.http.WebApplication;
import de.faustedition.document.Archive;
import de.faustedition.document.MaterialUnit;
import de.faustedition.graph.Graph;
import de.faustedition.Templates;
import eu.interedition.text.Anchor;
import eu.interedition.text.Layer;
import eu.interedition.text.Name;
import eu.interedition.text.TextConstants;
import eu.interedition.text.h2.H2TextRepository;
import org.codehaus.jackson.JsonNode;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;
import org.xml.sax.InputSource;

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
import javax.ws.rs.core.SecurityContext;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.Source;
import javax.xml.transform.sax.SAXSource;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Map;

import static eu.interedition.text.Query.text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/transcript")
public class TranscriptResource {


    private final GraphDatabaseService db;
    private final Transcripts transcripts;
    private final H2TextRepository<JsonNode> textRepository;
    private final Templates templates;

    @Inject
    public TranscriptResource(GraphDatabaseService db,
                              Transcripts transcripts,
                              H2TextRepository<JsonNode> textRepository,
                              Templates templates) {
        this.db = db;
        this.transcripts = transcripts;
        this.textRepository = textRepository;
        this.templates = templates;
    }

    @GET
    @Path("/{id}")
    public Response page(@PathParam("id") final long id, @Context final Request request, @Context final SecurityContext sc) {
        return Graph.execute(db, new Graph.Transaction<Response>() {
            @Override
            public Response execute(Graph graph) throws Exception {
                final MaterialUnit materialUnit = materialUnit(graph, id);
                if (materialUnit.getType() != MaterialUnit.Type.ARCHIVALDOCUMENT) {
                    // FIXME when requesting a transcript of a non-document, we loop endlessly in the graph in getArchive()!
                    throw new WebApplicationException(Response.status(Response.Status.BAD_REQUEST).entity("Only text transcripts can be displayed!").build());
                }

                final Archive archive = materialUnit.getArchive();

                final Map<String, Object> viewModel = Maps.newHashMap();
                viewModel.put("id", materialUnit.node.getId());
                viewModel.put("archiveName", (archive == null ? null : archive.getName()));
                viewModel.put("archiveId", (archive == null ? null : archive.getId()));
                viewModel.put("waId", materialUnit.getMetadataValue("wa-id"));
                viewModel.put("callnumber", materialUnit.getMetadataValue("callnumber"));
                return templates.render("transcript", viewModel, request, sc);
            }
        });
    }

    @GET
    @Path("/source/{id}")
    @Produces(MediaType.APPLICATION_XML)
    public Source xml(@PathParam("id") final long id) {
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
    }

    @GET
    @Path("/source/{id}")
    @Produces(MediaType.TEXT_PLAIN)
    public String plainText(@PathParam("id") final long id) {
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
    }

    @GET
    @Path("/source/{id}")
    @Produces(MediaType.APPLICATION_JSON)
    public Map<String, Object> json(@PathParam("id") final long id) {
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
    }

    protected MaterialUnit materialUnit(Graph graph, long materialUnitId) {
        try {
            return MaterialUnit.forNode(graph.db().getNodeById(materialUnitId));
        } catch (NotFoundException e) {
            throw notFound(materialUnitId);
        }
    }

    protected Layer<JsonNode> transcript(Graph graph, long materialUnitId) throws IOException, XMLStreamException {
        final Layer<JsonNode> transcript = transcripts.textOf(transcripts.transcriptOf(materialUnit(graph, materialUnitId)));
        if (transcript == null) {
            throw notFound(materialUnitId);
        }
        return transcript;
    }

    protected WebApplicationException notFound(long materialUnitId) {
        return new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(materialUnitId).build());
    }
}
