package de.faustedition.document;

import de.faustedition.Templates;
import de.faustedition.Database;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.DocumentRecord;
import org.jooq.DSLContext;

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

@Path("/document/{id}")
@Singleton
public class DocumentResource {

    private final Database database;
    private final Templates templates;

    @Inject
    public DocumentResource(Database database, Templates templates) {
        this.database = database;
        this.templates = templates;
    }

	@GET
    @Produces(MediaType.TEXT_HTML)
	public Response overview(@PathParam("id") final long id, @Context final Request request, @Context final SecurityContext sc) {
        return database.transaction(new Database.TransactionCallback<Response>() {
            @Override
            public Response doInTransaction(DSLContext sql) throws Exception {
                final DocumentRecord document = sql.selectFrom(Tables.DOCUMENT).where(Tables.DOCUMENT.ID.eq(id)).fetchOne();
                if (document == null) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(Long.toString(id)).build());
                }
                return templates.render(new Templates.ViewAndModel("document/document").add("document", document.intoMap()), request);
            }
        });
    }
}