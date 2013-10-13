package de.faustedition.genesis;

import com.google.common.collect.Iterables;
import de.faustedition.Templates;
import de.faustedition.document.Archive;
import de.faustedition.document.Document;
import de.faustedition.graph.Graph;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import java.util.Comparator;
import java.util.SortedSet;
import java.util.TreeSet;

@Path("/genesis")
@Singleton
public class GeneticGraphResource {

    private final Templates templates;
    private final Graph graph;

    @Inject
    public GeneticGraphResource(Templates templates, Graph graph) {
        this.templates = templates;
        this.graph = graph;
    }

    @GET
    public Response index(@Context final Request request) throws Exception {
        return graph.transaction(new Graph.TransactionCallback<Response>() {
            @Override
            public Response doInTransaction(Graph graph) throws Exception {
                final SortedSet<Document> archivalUnits = new TreeSet<Document>(new Comparator<Document>() {
                    @Override
                    public int compare(Document o1, Document o2) {
                        final String o1cn = o1.getMetadataValue("callnumber");
                        final String o2cn = o2.getMetadataValue("callnumber");
                        return (o1cn == null || o2cn == null) ? 0 : o1cn.compareTo(o2cn);
                    }
                });
                for (Archive archive : graph.getArchives()) {
                    for (Document document : Iterables.filter(archive, Document.class)) {
                        //temporallyPrecedes = document.geneticallyRelatedTo(MacrogeneticRelationManager.TEMP_PRE_REL);
                        archivalUnits.add(document);
                    }
                    //Iterables.addAll(archivalUnits, Iterables.filter(archive, Document.class));
                }
                return templates.render(request, new Templates.ViewAndModel("genesis/graph")
                        .add("archivalUnits", archivalUnits)
                );
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
