package de.faustedition.document;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.Templates;
import de.faustedition.db.Relations;
import de.faustedition.db.Tables;
import de.faustedition.db.tables.records.ArchiveRecord;
import org.jooq.DSLContext;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

@Path("/archive")
@Singleton
public class ArchiveResource {

    public static final FaustURI ARCHIVE_DESCRIPTOR_URI = new FaustURI(FaustAuthority.XML, "/archives.xml");

    private final DataSource dataSource;
    private final Templates templates;

    @Inject
    public ArchiveResource(DataSource dataSource, Templates templates) {
        this.dataSource = dataSource;
        this.templates = templates;
    }

    @GET
    public Response index(@Context final Request request) {
        return Relations.execute(dataSource, new Relations.Transaction<Response>() {
            @Override
            public Response execute(DSLContext sql) throws Exception {
                return templates.render(new Templates.ViewAndModel("document/archives")
                        .add("archives", sql.selectFrom(Tables.ARCHIVE).orderBy(Tables.ARCHIVE.NAME.asc()).fetchMaps()), request);
            }
        });
    }

    @Path("/{id}")
    @GET
    public Response archive(@PathParam("id") final String id, @Context final Request request) {
        return Relations.execute(dataSource, new Relations.Transaction<Response>() {
            @Override
            public Response execute(DSLContext sql) throws Exception {
                final ArchiveRecord archive = sql.selectFrom(Tables.ARCHIVE).where(Tables.ARCHIVE.LABEL.eq(id)).fetchOne();
                if (archive == null) {
                    return Response.status(Response.Status.NOT_FOUND).entity(id).build();
                }

                return templates.render(new Templates.ViewAndModel("document/archive")
                        .add("archive", archive.intoMap())
                        .add("documents", sql
                                .select(Tables.DOCUMENT.ID, Tables.DOCUMENT.CALLNUMBER, Tables.DOCUMENT.WA_ID)
                                .from(Tables.DOCUMENT)
                                .where(Tables.DOCUMENT.ARCHIVE_ID.eq(archive.getId()))
                                .orderBy(Tables.DOCUMENT.CALLNUMBER.asc())
                                .fetchMaps()
                        ), request);
            }
        });
    }
}
