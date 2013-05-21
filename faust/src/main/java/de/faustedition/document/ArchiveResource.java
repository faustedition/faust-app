package de.faustedition.document;

import com.google.common.collect.Maps;
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
import javax.ws.rs.core.SecurityContext;
import java.util.HashMap;
import java.util.Map;

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
    public Response index(@Context final Request request, @Context final SecurityContext sc) {
        return Relations.execute(dataSource, new Relations.Transaction<Response>() {
            @Override
            public Response execute(DSLContext sql) throws Exception {
                final Map<String, Object> model = new HashMap<String, Object>();
                model.put("archives", sql.selectFrom(Tables.ARCHIVE).orderBy(Tables.ARCHIVE.NAME.asc()).fetchMaps());
                return templates.render("document/archives", model, request, sc);
            }
        });
    }

    @Path("/{id}")
    @GET
    public Response archive(@PathParam("id") final String id, @Context final Request request, @Context final SecurityContext sc) {
        return Relations.execute(dataSource, new Relations.Transaction<Response>() {
            @Override
            public Response execute(DSLContext sql) throws Exception {
                final ArchiveRecord archive = sql.selectFrom(Tables.ARCHIVE).where(Tables.ARCHIVE.LABEL.eq(id)).fetchOne();
                if (archive == null) {
                    return Response.status(Response.Status.NOT_FOUND).entity(id).build();
                }

                final Map<String, Object> model = Maps.newHashMap();
                model.put("archive", archive.intoMap());
                model.put("documents", sql
                        .select(Tables.DOCUMENT.ID, Tables.DOCUMENT.CALLNUMBER, Tables.DOCUMENT.WA_ID)
                        .from(Tables.DOCUMENT)
                        .where(Tables.DOCUMENT.ARCHIVE_ID.eq(archive.getId()))
                        .orderBy(Tables.DOCUMENT.CALLNUMBER.asc())
                        .fetchMaps());
                return templates.render("document/archive", model, request, sc);
            }
        });
    }
}
