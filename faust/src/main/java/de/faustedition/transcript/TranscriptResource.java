package de.faustedition.transcript;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.Iterables;
import de.faustedition.Database;
import de.faustedition.Templates;
import de.faustedition.db.Tables;
import de.faustedition.text.TextAnnotationEnd;
import de.faustedition.text.TextAnnotationStart;
import de.faustedition.text.TextContent;
import de.faustedition.text.TextSegmentAnnotation;
import de.faustedition.text.TextSegmentAnnotationProcessor;
import de.faustedition.text.TextToken;
import org.jooq.DSLContext;

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
import javax.ws.rs.core.StreamingOutput;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Date;
import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/transcript")
public class TranscriptResource {

    private final Database database;
    private final Transcripts transcripts;
    private final Templates templates;
    private final ObjectMapper objectMapper;

    @Inject
    public TranscriptResource(Database database,
                              Transcripts transcripts,
                              Templates templates,
                              ObjectMapper objectMapper) {
        this.database = database;
        this.transcripts = transcripts;
        this.templates = templates;
        this.objectMapper = objectMapper;
    }

    @GET
    @Path("/{id}")
    public Response transcript(@Context Request request, @PathParam("id") final long id) throws Exception {
        return templates.render(request, transcriptData(request, id));
    }

    @GET
    @Path("/{id}/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Response transcriptData(@Context Request request, @PathParam("id") final long id) throws Exception {
        final Transcript transcript = transcript(id);
        final Date lastModified = transcript.lastModified();

        Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
        if (response == null) {
            final StringBuilder text = new StringBuilder();
            final ArrayNode annotations = objectMapper.createArrayNode();
            final Iterator<TextToken> tokens = new TextSegmentAnnotationProcessor(transcript.iterator());

            while (tokens.hasNext()) {
                final TextToken token = tokens.next();
                if (token instanceof TextSegmentAnnotation) {
                    final TextSegmentAnnotation annotation = (TextSegmentAnnotation) token;

                    final ObjectNode annotationDesc = objectMapper.createObjectNode()
                            .put("s", annotation.getSegment().lowerEndpoint())
                            .put("e", annotation.getSegment().upperEndpoint());
                    annotationDesc.put("d", annotation.getData());
                    annotations.add(annotationDesc);
                } else if (token instanceof TextContent) {
                    text.append(((TextContent) token).getContent());
                }
            }
            response = Response.ok(new Templates.ViewAndModel("transcript", lastModified)
                    .add("id", id)
                    .add("text", text.toString())
                    .add("annotations", annotations));
        }
        return response.lastModified(lastModified).build();
    }

    @GET
    @Path("/{id}/stream")
    @Produces(MediaType.APPLICATION_JSON)
    public Response transcriptStream(@Context Request request, @PathParam("id") final long id) {
        final Transcript transcript = transcript(id);
        final Date lastModified = transcript.lastModified();

        Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
        if (response == null) {
            response = Response.ok(new StreamingOutput() {
                @Override
                public void write(OutputStream output) throws IOException, WebApplicationException {
                    final JsonGenerator jgen = objectMapper.getJsonFactory().createJsonGenerator(output);
                    jgen.writeStartArray();
                    for (TextToken token : transcript) {
                        if (token instanceof TextContent) {
                            jgen.writeString(((TextContent) token).getContent());
                        } else if (token instanceof TextAnnotationStart) {
                            final TextAnnotationStart annotationStart = (TextAnnotationStart) token;
                            jgen.writeStartObject();
                            jgen.writeStringField("s", annotationStart.getId());
                            jgen.writeFieldName("d");
                            jgen.writeTree(annotationStart.getData());
                            jgen.writeEndObject();
                        } else if (token instanceof TextAnnotationEnd) {
                            jgen.writeStartObject();
                            jgen.writeStringField("e", ((TextAnnotationEnd) token).getId());
                            jgen.writeEndObject();
                        }
                    }
                    jgen.writeEndArray();
                    jgen.flush();
                }
            });
        }

        return response.lastModified(lastModified).build();
    }

    @GET
    @Path("/{id}/source")
    @Produces(MediaType.APPLICATION_XML)
    public Response transcriptSource(@Context Request request, @PathParam("id") final long id) {
        final Transcript transcript = transcript(id);
        final Date lastModified = transcript.lastModified();

        Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
        if (response == null) {
            response = Response.ok(Iterables.getOnlyElement(transcript.getSources()));
        }

        return response.lastModified(lastModified).build();
    }

    protected Transcript transcript(final long id) throws WebApplicationException {
        return database.transaction(new Database.TransactionCallback<Transcript>() {
            @Override
            public Transcript doInTransaction(DSLContext sql) throws Exception {
                if (sql.selectCount().from(Tables.TRANSCRIPT).where(Tables.TRANSCRIPT.ID.eq(id)).fetchOne().value1().intValue() == 0) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(id).build());
                }
                return transcripts.transcript(id);
            }

            @Override
            protected boolean rollsBackOn(Exception e) {
                return !(e instanceof WebApplicationException);
            }
        });
    }
}
