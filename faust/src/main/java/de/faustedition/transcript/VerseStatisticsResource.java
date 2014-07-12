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

package de.faustedition.transcript;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Ordering;
import de.faustedition.JsonRepresentationFactory;
import de.faustedition.SimpleVerseInterval;
import de.faustedition.VerseInterval;
import de.faustedition.document.Document;
import de.faustedition.document.MaterialUnit;
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
import org.springframework.ui.ModelMap;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VerseStatisticsResource extends ServerResource {

	@Autowired
	private GraphDatabaseService graphDb;

	@Autowired
	private VerseManager verseManager;

	@Autowired
	private JsonRepresentationFactory jsonRepresentationFactory;
	private ImmutableMap<MaterialUnit,Collection<GraphVerseInterval>> verseStatistics;
	private int from;
	private int to;

	@Override
	protected void doInit() throws ResourceException {

		super.doInit();
		from = Math.max(0, Integer.parseInt((String) getRequestAttributes().get("from")));
		to = Math.max(from, Integer.parseInt((String) getRequestAttributes().get("to")));
		if (from > to) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, "Invalid interval");
		}

		verseStatistics = verseManager.indexByMaterialUnit(verseManager.forInterval(new GraphVerseInterval(graphDb, from, to))).asMap();

	}

	@Get("json")
	public Representation chartData() {
		final List<Map<String, Object>> chartData = Lists.newLinkedList();
		final ImmutableMap<String, MaterialUnit> documentIndex = Maps.uniqueIndex(verseStatistics.keySet(), new Function<MaterialUnit, String>() {
			@Override
			public String apply(@Nullable MaterialUnit input) {				
				return input.toString() + " [" + input.node.getId() + "]";
			}
		});
		for (String documentDesc : Ordering.natural().immutableSortedCopy(documentIndex.keySet())) {
			final List<Map<String, Object>> intervals = Lists.newLinkedList();
			for (VerseInterval interval : Ordering.from(VerseManager.INTERVAL_COMPARATOR).immutableSortedCopy(verseStatistics.get(documentIndex.get(documentDesc)))) {
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
		return jsonRepresentationFactory.map(chartData, false);
	}
}
