package de.faustedition.transcript;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.faustedition.JsonRepresentationFactory;
import de.faustedition.VerseInterval;
import de.faustedition.document.MaterialUnit;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SceneStatisticsResource extends ServerResource {

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private GraphDatabaseService graphDb;

	@Autowired
	private JsonRepresentationFactory jsonRepresentationFactory;

	private SortedMap<VerseInterval,Integer> sceneStatistics;


	@Override
	protected void doInit() throws ResourceException {
		super.doInit();

		sceneStatistics = Maps.newTreeMap(VerseInterval.INTERVAL_COMPARATOR);
		try {
			for (VerseInterval scene : VerseInterval.scenesOf(Integer.parseInt((String) getRequestAttributes().get("part")))) {
				sceneStatistics.put(scene, 0);
			}
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
		} catch (IllegalArgumentException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e.getMessage());
		}

		final Session session = sessionFactory.getCurrentSession();
		final Map<MaterialUnit, Collection<TranscribedVerseInterval>> intervalIndex = TranscribedVerseInterval.indexByMaterialUnit(graphDb, TranscribedVerseInterval.all(session)).asMap();
		for (Map.Entry<MaterialUnit, Collection<TranscribedVerseInterval>> indexEntry : intervalIndex.entrySet()) {
			for (VerseInterval sceneInterval : sceneStatistics.keySet()) {
				for (TranscribedVerseInterval documentInterval : indexEntry.getValue()) {
					if (sceneInterval.overlapsWith(documentInterval)) {
						sceneStatistics.put(sceneInterval, sceneStatistics.get(sceneInterval) + 1);
						break;
					}
				}
			}

		}
	}

	@Get("json")
	public Representation chartData() {
		final List<Map<String, Object>> chartData = Lists.newArrayList();
		for (Map.Entry<VerseInterval, Integer> scene : sceneStatistics.entrySet()) {
			final Map<String, Object> sceneData = Maps.newLinkedHashMap();
			sceneData.put("scene", scene.getKey().getName());
			sceneData.put("documents", scene.getValue());
			chartData.add(sceneData);
		}
		return jsonRepresentationFactory.map(chartData, false);
	}
}
