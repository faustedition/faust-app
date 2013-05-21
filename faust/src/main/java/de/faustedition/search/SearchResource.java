package de.faustedition.search;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.faustedition.document.Document;
import de.faustedition.graph.Graph;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/search")
@Singleton
public class SearchResource {

	private final GraphDatabaseService graphDatabaseService;

    @Inject
    public SearchResource(GraphDatabaseService graphDatabaseService) {
        this.graphDatabaseService = graphDatabaseService;
    }

    @Path("/{query}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
	public Map<String,Object> searchResults(@PathParam("query") final String query) throws Exception {
        return Graph.execute(graphDatabaseService, new Graph.Transaction<Map<String, Object>>() {
            @Override
            public Map<String, Object> execute(Graph graph) throws Exception {
                final List<Map<String, Object>> documentDescs = Lists.newArrayList();

                final List<Document> documents = query(graph.db(), query);
                if (documents.isEmpty()) {
                    final String wildCardQuery = new StringBuilder("*")
                            .append(query.replaceAll("\\*", "").toLowerCase())
                            .append("*")
                            .toString();

                    documents.addAll(query(graph.db(), wildCardQuery));
                }

                for (Document document : documents) {
                    final Map<String, Object> documentDesc = Maps.newHashMap();
                    documentDesc.put("id", document.node.getId());
                    documentDesc.put("callnumbers", toSortedValues(document.getMetadata("callnumber")));
                    documentDesc.put("waIds", toSortedValues(document.getMetadata("wa-id")));
                    documentDesc.put("uris", toSortedValues(document.getMetadata("uri")));
                    documentDescs.add(documentDesc);
                }

                final Map<String,Object> results = Maps.newHashMap();
                results.put("documents", documentDescs);
                return results;
            }

            private List<Document> query(GraphDatabaseService db, String term) {
                return Lists.newArrayList(Iterables.limit(Document.find(db, term), 25));
            }

            private SortedSet<String> toSortedValues(String[] metadata) {
                return Sets.newTreeSet(Arrays.asList(Objects.firstNonNull(metadata, new String[0])));
            }
        });
	}


}
