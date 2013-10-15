package de.faustedition.document;

import de.faustedition.Database;
import de.faustedition.Templates;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.ArchiveRecord;
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
import java.util.List;
import java.util.Map;

@Path("/archive")
@Singleton
public class ArchiveResource {

    private final Database database;
    private final Templates templates;

    @Inject
    public ArchiveResource(Database database, Templates templates) {
        this.database = database;
        this.templates = templates;
    }

    @GET
    public Response indexView(@Context final Request request) {
        return database.transaction(new Database.TransactionCallback<Response>() {
            @Override
            public Response doInTransaction(DSLContext sql) throws Exception {
                return templates.render(request, new Templates.ViewAndModel("document/archives").add("archives", index()));
            }
        });
    }

    @GET
    @Path("/data")
    @Produces(MediaType.APPLICATION_JSON)
    public List<Map<String, Object>> index() {
        return database.transaction(new Database.TransactionCallback<List<Map<String, Object>>>() {
            @Override
            public List<Map<String, Object>> doInTransaction(DSLContext sql) throws Exception {
                return sql.selectFrom(Tables.ARCHIVE).orderBy(Tables.ARCHIVE.NAME.asc()).fetchMaps();
            }
        });
    }

    @Path("/{id}")
    @GET
    public Response archiveView(@PathParam("id") final String id, @Context final Request request) {
        return templates.render(request, archive(id));
    }

    @GET
    @Path("/{id}/data")
    @Produces(MediaType.APPLICATION_JSON)
    public Templates.ViewAndModel archive(@PathParam("id") final String id) {
        return database.transaction(new Database.TransactionCallback<Templates.ViewAndModel>() {
            @Override
            public Templates.ViewAndModel doInTransaction(DSLContext sql) throws Exception {
                final ArchiveRecord archive = sql.selectFrom(Tables.ARCHIVE).where(Tables.ARCHIVE.LABEL.eq(id)).fetchOne();
                if (archive == null) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(id).build());
                }

                return new Templates.ViewAndModel("document/archive")
                        .add("archive", archive.intoMap())
                        .add("documents", sql
                                .select(Tables.DOCUMENT.ID, Tables.DOCUMENT.CALLNUMBER, Tables.DOCUMENT.WA_ID)
                                .from(Tables.DOCUMENT)
                                .where(Tables.DOCUMENT.ARCHIVE_ID.eq(archive.getId()))
                                .orderBy(Tables.DOCUMENT.CALLNUMBER.asc())
                                .fetchMaps()
                        );
            }

            @Override
            protected boolean rollsBackOn(Exception e) {
                return !(e instanceof WebApplicationException);
            }
        });
    }
}
