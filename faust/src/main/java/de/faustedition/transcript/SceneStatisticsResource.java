package de.faustedition.transcript;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.faustedition.Database;
import de.faustedition.document.MaterialUnit;
import de.faustedition.graph.Graph;
import de.faustedition.text.VerseInterval;
import org.neo4j.graphdb.GraphDatabaseService;

import javax.inject.Inject;
import javax.sql.DataSource;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/transcript/by-scene/{part}")
public class SceneStatisticsResource {

	private final Database database;
    private final Graph graph;


    @Inject
    public SceneStatisticsResource(Database database, Graph graph) {
        this.database = database;
        this.graph = graph;
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
	public List<Map<String, Object>> sceneStatistics(@PathParam("part") int part) {
        final SortedMap<VerseInterval,Integer> sceneStatistics = Maps.newTreeMap(VerseInterval.INTERVAL_COMPARATOR);
        for (VerseInterval scene : VerseInterval.scenesOf(part)) {
            sceneStatistics.put(scene, 0);
        }

        // FIXME: query database
        for (Map.Entry<MaterialUnit, Collection<VerseInterval>> indexEntry : Collections.<MaterialUnit, Collection<VerseInterval>>emptyMap().entrySet()) {
			for (VerseInterval sceneInterval : sceneStatistics.keySet()) {
				for (VerseInterval documentInterval : indexEntry.getValue()) {
					if (sceneInterval.overlapsWith(documentInterval)) {
						sceneStatistics.put(sceneInterval, sceneStatistics.get(sceneInterval) + 1);
						break;
					}
				}
			}

		}
        final List<Map<String, Object>> chartData = Lists.newArrayList();
        for (Map.Entry<VerseInterval, Integer> scene : sceneStatistics.entrySet()) {
            final Map<String, Object> sceneData = Maps.newLinkedHashMap();
            sceneData.put("scene", scene.getKey().getName());
            sceneData.put("documents", scene.getValue());
            chartData.add(sceneData);
        }
        return chartData;
	}
}
