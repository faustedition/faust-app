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

package de.faustedition.search;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.faustedition.JsonRepresentationFactory;
import de.faustedition.document.Document;
import org.neo4j.graphdb.GraphDatabaseService;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SearchResource extends ServerResource {

	private static final Logger LOG = LoggerFactory.getLogger(SearchResource.class);

	@Autowired
	private JsonRepresentationFactory jsonFactory;

	@Autowired
	private GraphDatabaseService db;

	private String searchTerm;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		searchTerm = Reference.decode(Objects.firstNonNull((String) getRequest().getAttributes().get("term"), ""));
		if (searchTerm.isEmpty()) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	@Get("json")
	public Representation results() {
		if (LOG.isTraceEnabled()) {
			LOG.trace("Searching for '{}'", searchTerm);
		}
		final Map<String,Object> results = Maps.newHashMap();

		final List<Map<String, Object>> documentDescs = Lists.newArrayList();
		results.put("documents", documentDescs);

		List<Document> documents = query(searchTerm);
		if (documents.isEmpty()) {
			documents = query(toQuery(searchTerm));
		}
		for (Document document : documents) {
			final Map<String, Object> documentDesc = Maps.newHashMap();
			documentDesc.put("id", document.node.getId());
			documentDesc.put("callnumbers", toSortedValues(document.getMetadata("callnumber")));
			documentDesc.put("waIds", toSortedValues(document.getMetadata("wa-id")));
			documentDesc.put("uris", toSortedValues(document.getMetadata("uri")));		
			documentDescs.add(documentDesc);
		}
		return jsonFactory.map(results, false);
	}

	private List<Document> query(String term) {
		return Lists.newArrayList(Iterables.limit(Document.find(db, term), 25));
	}

	public static String toQuery(String search) {
		return new StringBuilder("*").append(search.replaceAll("\\*", "").toLowerCase()).append("*").toString();
	}

	private static SortedSet<String> toSortedValues(String[] metadata) {
		return Sets.newTreeSet(Arrays.asList(Objects.firstNonNull(metadata, new String[0])));
	}	
	
}
