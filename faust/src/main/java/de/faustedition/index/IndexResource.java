package de.faustedition.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Stopwatch;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.Query;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import java.util.Collections;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.faustedition.index.AnnotationFilteringIndexSearcher.Result;
import static de.faustedition.index.TranscriptTokenAnnotation.HAND_GOETHE;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/index")
@Singleton
public class IndexResource {

    private static final Logger LOG = Logger.getLogger(IndexResource.class.getName());

    private final Index index;
    private final ObjectMapper objectMapper;

    @Inject
    public IndexResource(Index index, ObjectMapper objectMapper) {
        this.index = index;
        this.objectMapper = objectMapper;
    }

    @Path("/{query}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ObjectNode search(@PathParam("query") final String queryStr,
                            @QueryParam("page") @DefaultValue("0") final int page,
                            @QueryParam("pageSize") @DefaultValue("10") final int pageSize) throws Exception {
        try {
            return index.transaction(new Index.TransactionCallback<ObjectNode>() {
                @Override
                public ObjectNode doInTransaction() throws Exception {
                    final Stopwatch stopwatch = Stopwatch.createStarted();

                    final Query query = Index.queryParser("documentary").parse(queryStr);
                    final int pageOffset = (page * pageSize);

                    final IndexSearcher searcher = searcher();
                    final AnnotationFilteringIndexSearcher filteringSearcher = new AnnotationFilteringIndexSearcher(searcher, 100, 3);

                    final List<Result> results = Lists.newLinkedList();
                    final int totalHits = filteringSearcher.search(
                            query,
                            results,
                            (pageOffset + pageSize),
                            TranscriptTokenAnnotation.encode(HAND_GOETHE),
                            "documentary"
                    );
                    final ObjectNode resultNode = objectMapper.createObjectNode()
                            .put("query", queryStr)
                            .put("page", page)
                            .put("pageSize", pageSize)
                            .put("total", totalHits);

                    final ArrayNode hits = resultNode.putArray("results");
                    final List<String> explanations = Lists.newArrayListWithCapacity(pageSize);
                    for (Result result : Iterables.skip(results, pageOffset)) {
                        final org.apache.lucene.document.Document indexDocument = searcher.doc(result.getDoc());

                        final String documentId = indexDocument.get("id");
                        final String archive = indexDocument.get("archive");
                        final String callnumber = indexDocument.get("callnumber");

                        hits.addObject()
                                .put("id", documentId)
                                .put("archive", archive)
                                .put("callnumber", callnumber)
                                .put("score", result.getScore())
                                .put("excerpt", Iterables.toString(result.getKwicSegments().entrySet()));

                        if (LOG.isLoggable(Level.FINER)) {
                            explanations.add(Joiner.on(" => ").join(
                                    String.format("%s/%s[#%s]", archive, callnumber, documentId),
                                    searcher.explain(query, result.getDoc()).toString()
                            ));
                        }
                    }

                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine(Joiner.on("\n").join(
                                String.format("Query: '%s'", queryStr),
                                String.format("Time: %s", stopwatch.stop())
                        ));
                        if (LOG.isLoggable(Level.FINER)) {
                            LOG.fine(Joiner.on("\n").join(Iterables.concat(
                                    Collections.singleton("Explanations:"),
                                    explanations
                            )));
                        }
                    }

                    return resultNode;
                }
            });
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Index error", e);
            throw e;
        }
    }


}
