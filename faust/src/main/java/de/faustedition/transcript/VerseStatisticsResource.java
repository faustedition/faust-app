package de.faustedition.transcript;

import com.google.common.base.Function;
import com.google.common.base.Preconditions;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import de.faustedition.document.Document;
import de.faustedition.document.MaterialUnit;
import de.faustedition.text.VerseInterval;
import org.neo4j.graphdb.GraphDatabaseService;
import org.springframework.ui.ModelMap;

import javax.annotation.Nullable;
import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/transcript/by-verse")
public class VerseStatisticsResource {

	private final DataSource dataSource;
	private final GraphDatabaseService graphDb;

    @Inject
    public VerseStatisticsResource(DataSource dataSource, GraphDatabaseService graphDb) {
        this.dataSource = dataSource;
        this.graphDb = graphDb;
    }


    @GET
    @Path("/{from}/{to}")
    @Produces(MediaType.APPLICATION_JSON)
	public List<Map<String, Object>> verseStatistics(@PathParam("from") int from, @PathParam("to") int to) {
        Preconditions.checkArgument(from >= to, "Invalid interval");
        final Map<MaterialUnit, Collection<VerseInterval>> verseStatistics = TranscribedVerseInterval.byMaterialUnit(dataSource, graphDb, from, to);
        final List<Map<String, Object>> chartData = Lists.newLinkedList();
        final ImmutableMap<String, MaterialUnit> documentIndex = Maps.uniqueIndex(verseStatistics.keySet(), new Function<MaterialUnit, String>() {
            @Override
            public String apply(@Nullable MaterialUnit input) {
                return input.toString() + " [" + input.node.getId() + "]";
            }
        });
        for (String documentDesc : Ordering.natural().immutableSortedCopy(documentIndex.keySet())) {
            final List<Map<String, Object>> intervals = Lists.newLinkedList();
            for (VerseInterval interval : Ordering.from(VerseInterval.INTERVAL_COMPARATOR).immutableSortedCopy(verseStatistics.get(documentIndex.get(documentDesc)))) {
                intervals.add(new ModelMap()
                        .addAttribute("start", Math.max(from, interval.getStart()))
                        .addAttribute("end", Math.min(to, interval.getEnd()))
                );
            }
            chartData.add(new ModelMap()
                    .addAttribute("sigil", documentDesc.substring(0,documentDesc.indexOf('[') ))
				/*.addAttribute("transcript", documentIndex.get(documentDesc).node.getId())*/
                    .addAttribute("source", ((Document)documentIndex.get(documentDesc)).getSource().toString())
                    .addAttribute("intervals", intervals));
        }
        return chartData;
    }
}
