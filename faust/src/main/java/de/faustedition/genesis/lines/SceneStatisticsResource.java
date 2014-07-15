/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.genesis.lines;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.faustedition.JsonRepresentationFactory;
import org.hibernate.SessionFactory;
import org.neo4j.graphdb.GraphDatabaseService;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

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
		/*
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
		final Map<MaterialUnit, Collection<VerseInterval>> intervalIndex = VerseInterval.indexByMaterialUnit(graphDb, VerseInterval.all(session)).asMap();
		for (Map.Entry<MaterialUnit, Collection<VerseInterval>> indexEntry : intervalIndex.entrySet()) {
			for (VerseInterval sceneInterval : sceneStatistics.keySet()) {
				for (VerseInterval documentInterval : indexEntry.getValue()) {
					if (sceneInterval.overlapsWith(documentInterval)) {
						sceneStatistics.put(sceneInterval, sceneStatistics.get(sceneInterval) + 1);
						break;
					}
				}
			}

		}
		*/
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
