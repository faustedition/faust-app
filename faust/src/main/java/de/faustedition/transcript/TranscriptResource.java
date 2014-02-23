package de.faustedition.transcript;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.collect.Iterables;
import de.faustedition.Database;
import de.faustedition.Templates;
import de.faustedition.db.Tables;
import eu.interedition.text.stream.TextAnnotationEnd;
import eu.interedition.text.stream.TextAnnotationStart;
import eu.interedition.text.stream.TextContent;
import eu.interedition.text.stream.TextToken;
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
import java.util.Date;

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
    public Response transcriptData(@Context Request request, @PathParam("id") final long id) {
        final Transcript transcript = transcript(id);
        final Date lastModified = transcript.lastModified();

        Response.ResponseBuilder response = request.evaluatePreconditions(lastModified);
        if (response == null) {
            response = Response.ok(new Templates.ViewAndModel("transcript", lastModified)
                    .add("id", id)
                    .add("text", json(transcript)));
        }
        return response.lastModified(lastModified).build();
    }

    protected ArrayNode json(Transcript transcript) {
        final ArrayNode text = objectMapper.createArrayNode();
        for (TextToken token : transcript) {
            if (token instanceof TextContent) {
                final String content = ((TextContent) token).getContent();
                if (content.length() > 0) {
                    text.add(content);
                }
            } else if (token instanceof TextAnnotationStart) {
                final TextAnnotationStart annotationStart = (TextAnnotationStart) token;
                text.addObject().put("s", annotationStart.getId()).put("d", annotationStart.getData());
            } else if (token instanceof TextAnnotationEnd) {
                text.addObject().put("e", ((TextAnnotationEnd) token).getId());
            }
        }
        return text;
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
