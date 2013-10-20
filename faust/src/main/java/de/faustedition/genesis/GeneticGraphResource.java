package de.faustedition.genesis;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import de.faustedition.Database;
import de.faustedition.Templates;
import de.faustedition.db.Tables;
import org.jooq.DSLContext;
import org.jooq.Record1;
import org.jooq.Result;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;

@Path("/genesis")
@Singleton
public class GeneticGraphResource {

    private final Templates templates;
    private final Database database;
    private final ObjectMapper objectMapper;

    @Inject
    public GeneticGraphResource(Templates templates, Database database, ObjectMapper objectMapper) {
        this.templates = templates;
        this.database = database;
        this.objectMapper = objectMapper;
    }

    @GET
    public Response index(@Context final Request request) throws Exception {
        return database.transaction(new Database.TransactionCallback<Response>() {
            @Override
            public Response doInTransaction(DSLContext sql) throws Exception {
                final ArrayNode metadataNode = objectMapper.createArrayNode();
                final Result<Record1<String>> metadata = sql.select(Tables.DOCUMENT.METADATA).from(Tables.DOCUMENT).orderBy(Tables.DOCUMENT.CALLNUMBER).fetch();
                for (Record1<String> metadataRecord : metadata) {
                    metadataNode.add(objectMapper.reader().readTree(metadataRecord.value1()));
                }
                return templates.render(request, new Templates.ViewAndModel("genesis/graph").add("archivalUnits", metadataNode));
            }
        });
    }

    @Path("/work/")
    public Response work(@Context final Request request) {
        return templates.render(request, new Templates.ViewAndModel("genesis/work"));
    }

    @Path("/app/")
    public Response app(@Context final Request request) {
        return templates.render(request, new Templates.ViewAndModel("genesis/app"));
    }
}
