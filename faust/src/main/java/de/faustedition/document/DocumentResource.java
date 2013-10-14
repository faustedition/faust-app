package de.faustedition.document;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import de.faustedition.Database;
import de.faustedition.Templates;
import de.faustedition.db.Tables;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Record3;
import org.jooq.Result;

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


@Path("/document")
@Singleton
public class DocumentResource {

    private final Database database;
    private final Templates templates;
    private final ObjectMapper objectMapper;

    @Inject
    public DocumentResource(Database database, Templates templates, ObjectMapper objectMapper) {
        this.database = database;
        this.templates = templates;
        this.objectMapper = objectMapper;
    }

    @GET
    @Path("/{id}")
    public Response documentView(@Context Request request, @PathParam("id") final long id) {
        return templates.render(request, document(id));
    }

	@GET
    @Path("/{id}/data")
    @Produces(MediaType.APPLICATION_JSON)
	public Templates.ViewAndModel document(@PathParam("id") final long id) {
        return database.transaction(new Database.TransactionCallback<Templates.ViewAndModel>() {
            @Override
            public Templates.ViewAndModel doInTransaction(DSLContext sql) throws Exception {
                final Record1<String> document = sql.select(Tables.DOCUMENT.METADATA)
                        .from(Tables.DOCUMENT)
                        .where(Tables.DOCUMENT.ID.eq(id))
                        .fetchOne();

                if (document == null) {
                    throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(id).build());
                }

                final Result<Record3<Long,Long,String>> materialUnits = sql
                        .select(Tables.MATERIAL_UNIT.ID, Tables.MATERIAL_UNIT.TRANSCRIPT_ID, Tables.FACSIMILE.PATH)
                        .from(Tables.MATERIAL_UNIT)
                        .leftOuterJoin(Tables.FACSIMILE)
                        .on(Tables.FACSIMILE.MATERIAL_UNIT_ID.eq(Tables.MATERIAL_UNIT.ID))
                        .where(Tables.MATERIAL_UNIT.DOCUMENT_ID.eq(id))
                        .orderBy(Tables.MATERIAL_UNIT.DOCUMENT_ORDER.asc(), Tables.FACSIMILE.FACSIMILE_ORDER.asc())
                        .fetch();

                final ArrayNode pages = objectMapper.createArrayNode();
                ObjectNode materialUnit;
                ArrayNode facsimiles = null;
                long currentUnit = 0;
                for (Record3<Long,Long,String> materialUnitData : materialUnits) {
                    if (materialUnitData.value1() != currentUnit) {
                        currentUnit = materialUnitData.value1();
                        materialUnit = pages.addObject();
                        facsimiles = materialUnit.putArray("facsimiles");
                        if (materialUnitData.value2() != null) {
                            materialUnit.put("transcript", materialUnitData.value2());
                        }
                    }
                    if (materialUnitData.value3() != null) {
                        facsimiles.add(materialUnitData.value3());
                    }
                }

                return new Templates.ViewAndModel("document/document-app")
                        .add("document", objectMapper.reader().readTree(document.value1()))
                        .add("pages", pages);
            }

            @Override
            protected boolean rollsBackOn(Exception e) {
                return !(e instanceof WebApplicationException);
            }
        });
	}
}
