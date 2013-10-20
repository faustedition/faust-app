package de.faustedition.transcript;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.faustedition.Database;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Templates;
import de.faustedition.db.Tables;
import de.faustedition.text.Annotation;
import de.faustedition.text.AnnotationProcessor;
import de.faustedition.text.Characters;
import de.faustedition.text.Token;
import de.faustedition.xml.Sources;
import org.jooq.DSLContext;
import org.jooq.Record1;

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
import java.io.File;
import java.util.Arrays;
import java.util.Iterator;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/transcript")
public class TranscriptResource {

    private final Database database;
    private final Sources sources;
    private final Transcripts transcripts;
    private final Templates templates;
    private final ObjectMapper objectMapper;

    @Inject
    public TranscriptResource(Database database,
                              Sources sources,
                              Transcripts transcripts,
                              Templates templates,
                              ObjectMapper objectMapper) {
        this.database = database;
        this.sources = sources;
        this.transcripts = transcripts;
        this.templates = templates;
        this.objectMapper = objectMapper;
    }

    @GET
    @Path("/{id}")
    public Response transcript(@Context Request request, @PathParam("id") final long id) throws Exception {
        return templates.render(request, transcriptData(id));
    }

    @GET
    @Path("/{id}/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Templates.ViewAndModel transcriptData(@PathParam("id") final long id) throws Exception {
        checkTranscriptExists(id);

        return transcripts.tokens(Arrays.asList(id), new Transcripts.TokenCallback<Templates.ViewAndModel>() {
            @Override
            public Templates.ViewAndModel withTokens(Iterator<Token> tokens) throws Exception {

                tokens = new AnnotationProcessor(tokens);

                final StringBuilder text = new StringBuilder();
                final ArrayNode annotations = objectMapper.createArrayNode();

                while (tokens.hasNext()) {
                    final Token token = tokens.next();
                    if (token instanceof Annotation) {
                        final Annotation annotation = (Annotation) token;

                        final ObjectNode annotationDesc = objectMapper.createObjectNode()
                                .put("s", annotation.getSegment().lowerEndpoint())
                                .put("e", annotation.getSegment().upperEndpoint());
                        annotationDesc.put("d", annotation.getData());
                        annotations.add(annotationDesc);
                    } else if (token instanceof Characters) {
                        text.append(((Characters) token).getContent());
                    }
                }
                return new Templates.ViewAndModel("transcript")
                        .add("id", id)
                        .add("text", text.toString())
                        .add("annotations", annotations);
            }
        });
    }

    @GET
    @Path("/{id}/source")
    @Produces(MediaType.APPLICATION_XML)
    public File transcriptSource(@PathParam("id") final long id) {
        return database.transaction(new Database.TransactionCallback<File>() {
            @Override
            public File doInTransaction(DSLContext sql) throws Exception {
                final Record1<String> transcriptText = sql.select(Tables.TRANSCRIPT.SOURCE_URI).from(Tables.TRANSCRIPT).where(Tables.TRANSCRIPT.ID.eq(id)).fetchOne();
                if (transcriptText == null) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(id).build());
                }

                final FaustURI uri = new FaustURI(FaustAuthority.XML, "/" + transcriptText.value1());
                if (!sources.isResource(uri)) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(uri.toString()).build());
                }

                return sources.file(uri);
            }

            @Override
            protected boolean rollsBackOn(Exception e) {
                return !(e instanceof WebApplicationException);
            }
        });
    }

    protected void checkTranscriptExists(final long id) throws WebApplicationException {
        database.transaction(new Database.TransactionCallback<Object>() {
            @Override
            public Object doInTransaction(DSLContext sql) throws Exception {
                if (sql.selectCount().from(Tables.TRANSCRIPT).where(Tables.TRANSCRIPT.ID.eq(id)).fetchOne().value1().intValue() == 0) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(id).build());
                }
                return null;
            }

            @Override
            protected boolean rollsBackOn(Exception e) {
                return !(e instanceof WebApplicationException);
            }
        });
    }
}
