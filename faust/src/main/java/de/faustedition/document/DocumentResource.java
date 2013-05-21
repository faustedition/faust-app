package de.faustedition.document;

import de.faustedition.Templates;
import de.faustedition.db.Relations;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.DocumentRecord;
import org.codehaus.jackson.map.ObjectMapper;
import org.jooq.DSLContext;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
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
import java.util.HashMap;
import java.util.Map;

@Path("/document/{id}")
@Singleton
public class DocumentResource {

    private final DataSource dataSource;
    private final Templates templates;

    @Inject
    public DocumentResource(DataSource dataSource, Templates templates) {
        this.dataSource = dataSource;
        this.templates = templates;
    }

	@GET
    @Produces(MediaType.TEXT_HTML)
	public Response overview(@PathParam("id") final long id, @Context final Request request, @Context final SecurityContext sc) {
        return Relations.execute(dataSource, new Relations.Transaction<Response>() {
            @Override
            public Response execute(DSLContext sql) throws Exception {
                final DocumentRecord document = document(sql, id);
                final Map<String, Object> viewModel = new HashMap<String, Object>();
                viewModel.put("document", document.intoMap());
                return templates.render("document/document", viewModel, request, sc);
            }
        });
    }

    protected DocumentRecord document(DSLContext sql, long id) {
        final DocumentRecord document = sql.selectFrom(Tables.DOCUMENT).where(Tables.DOCUMENT.ID.eq(id)).fetchOne();
        if (document == null) {
            throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(Long.toBinaryString(id)).build());
        }
        return document;
    }
}