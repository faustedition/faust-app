package de.faustedition.transcript;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.faustedition.text.VerseInterval;
import de.faustedition.document.MaterialUnit;
import org.neo4j.graphdb.GraphDatabaseService;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;

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
import java.util.SortedMap;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Path("/transcript/by-scene/")
public class SceneStatisticsResource {

	private final DataSource dataSource;
	private final GraphDatabaseService graphDatabaseService;

    @Inject
    public SceneStatisticsResource(DataSource dataSource, GraphDatabaseService graphDatabaseService) {
        this.dataSource = dataSource;
        this.graphDatabaseService = graphDatabaseService;
    }


    @GET
    @Path("{part}")
    @Produces(MediaType.APPLICATION_JSON)
	public List<Map<String, Object>> sceneStatistics(@PathParam("part") int part) {
        final SortedMap<VerseInterval,Integer> sceneStatistics = Maps.newTreeMap(VerseInterval.INTERVAL_COMPARATOR);
        for (VerseInterval scene : VerseInterval.scenesOf(part)) {
            sceneStatistics.put(scene, 0);
        }

        for (Map.Entry<MaterialUnit, Collection<VerseInterval>> indexEntry : TranscribedVerseInterval.byMaterialUnit(dataSource, graphDatabaseService, 0, Integer.MAX_VALUE).entrySet()) {
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
