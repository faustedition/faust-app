package de.faustedition.genesis;

import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import de.faustedition.WebApplication;
import de.faustedition.document.Archive;
import de.faustedition.document.Document;
import de.faustedition.graph.Graph;
import de.faustedition.template.Templates;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.Request;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.SecurityContext;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

@Path("/genesis")
@Singleton
public class GeneticGraphResource {

    private final Templates templates;
    private final GraphDatabaseService graphDatabaseService;

    @Inject
    public GeneticGraphResource(Templates templates, GraphDatabaseService graphDatabaseService) {
        this.templates = templates;
        this.graphDatabaseService = graphDatabaseService;
    }

    @GET
    public Response index(@Context final Request request, @Context final SecurityContext sc) {
        try {
            return Graph.execute(graphDatabaseService, new Graph.Transaction<Response>() {
                @Override
                public Response execute(Graph graph) throws Exception {
                    final Map<String, Object> model = new HashMap<String, Object>();
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
                    model.put("archivalUnits", archivalUnits);
                    return templates.render("genesis/graph", model, request, sc);
                }
            });
        } catch (Throwable t) {
            throw WebApplication.propagateExceptions(t);
        }
    }

    @Path("/work/")
    public Response work(@Context final Request request, @Context final SecurityContext sc) {
        return templates.render("genesis/work", Maps.<String, Object>newHashMap(), request, sc);
    }

    @Path("/app/")
    public Response app(@Context final Request request, @Context final SecurityContext sc) {
        return templates.render("genesis/app", Maps.<String, Object>newHashMap(), request, sc);
    }
}
