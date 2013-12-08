package de.faustedition.index;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
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
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.faustedition.index.AnnotationFilteringIndexSearcher.Result;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/index")
@Singleton
public class IndexResource {

    private static final Logger LOG = Logger.getLogger(IndexResource.class.getName());

    private final Index index;
    private final TranscriptExcerpts transcriptExcerpts;
    private final TranscriptTokenAnnotationCodec annotationCodec;
    private final ObjectMapper objectMapper;

    @Inject
    public IndexResource(Index index, TranscriptExcerpts transcriptExcerpts, TranscriptTokenAnnotationCodec annotationCodec, ObjectMapper objectMapper) {
        this.index = index;
        this.transcriptExcerpts = transcriptExcerpts;
        this.annotationCodec = annotationCodec;
        this.objectMapper = objectMapper;
    }

    @Path("/{query}")
    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public ArrayNode search(@PathParam("query") final String queryStr,
                            @QueryParam("page") @DefaultValue("0") final int page,
                            @QueryParam("pageSize") @DefaultValue("10") final int pageSize) throws Exception {
        try {
            return index.transaction(new Index.TransactionCallback<ArrayNode>() {
                @Override
                public ArrayNode doInTransaction() throws Exception {
                    final Stopwatch stopwatch = Stopwatch.createStarted();

                    final Query query = Index.queryParser("textual").parse(queryStr);
                    final int pageOffset = (page * pageSize);

                    final IndexSearcher searcher = searcher();
                    final AnnotationFilteringIndexSearcher indexSearcher = new AnnotationFilteringIndexSearcher(searcher, 20, 3);

                    final List<Result> resultList = indexSearcher.search(query, (pageOffset + pageSize), "textual");
                    final ArrayNode results = objectMapper.createArrayNode();
                    final List<String> explanations = Lists.newArrayListWithCapacity(pageSize);
                    for (Result result : Iterables.skip(resultList, pageOffset)) {
                        final org.apache.lucene.document.Document indexDocument = searcher.doc(result.getDoc());

                        final String documentId = indexDocument.get("id");
                        final String archive = indexDocument.get("archive");
                        final String callnumber = indexDocument.get("callnumber");
                        /*
                        final List<String> excerpts = Lists.newArrayListWithCapacity(3);
                        for (TranscriptExcerpts.TranscriptExcerpt excerpt : transcriptExcerpts.get(query, indexReader, scoreDoc.doc, "textual", 20, 3)) {
                            excerpts.add(excerpt.getExcerpt().replaceAll("\n", "\u00b6"));
                        }
                        */

                        results.addObject()
                                .put("id", documentId)
                                .put("archive", archive)
                                .put("callnumber", callnumber)
                                .put("score", result.getScore())
                                .put("excerpt", Iterables.toString(result.getKwicSegments().entrySet()));

                        if (LOG.isLoggable(Level.FINE)) {
                            explanations.add(Joiner.on(" => ").join(
                                    String.format("%s/%s[#%s]", archive, callnumber, documentId),
                                    searcher.explain(query, result.getDoc()).toString()
                            ));
                        }
                    }

                    if (LOG.isLoggable(Level.FINE)) {
                        LOG.fine(Joiner.on("\n").join(Iterables.concat(
                                Arrays.asList(
                                        String.format("Query: '%s'", queryStr),
                                        String.format("Time: %s", stopwatch.stop()),
                                        String.format("Explanations:")
                                ),
                                explanations
                        )));
                    }

                    return results;
                }
            });
        } catch (Exception e) {
            LOG.log(Level.SEVERE, "Index error", e);
            throw e;
        }
    }


}
