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
import de.faustedition.transcript.TranscriptManager;
import de.faustedition.transcript.VerseManager;
import eu.interedition.text.Anchor;
import eu.interedition.text.Layer;
import eu.interedition.text.neo4j.LayerNode;
import org.codehaus.jackson.JsonNode;
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

import java.io.IOException;
import java.util.*;

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

	@Autowired
	private VerseManager verseManager;

	@Autowired
	private TranscriptManager transcriptManager;

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

		List<Document> documents = idnoQuery(searchTerm);
		if (documents.isEmpty()) {
			documents = idnoQuery(toQuery(searchTerm));
		}
		for (Document document : documents) {
			final Map<String, Object> documentDesc = documentDescMap(document);
			documentDescs.add(documentDesc);
		}

		List<LayerNode<JsonNode>> lines = fulltextQuery(searchTerm);
		for (Layer<JsonNode> line : lines) {
			Anchor<JsonNode> anchor = Iterables.getOnlyElement(line.getAnchors());
			LayerNode<JsonNode> transcript = (LayerNode<JsonNode>) anchor.getText();

			try {
				String verseText = anchor.getText().read(anchor.getRange());
				LOG.trace("Found verse: " + verseText);
				Document document = ((Document) transcriptManager.materialUnitForTranscript(transcript));

				final Map<String, Object> documentDesc = documentDescMap(document);
				documentDesc.put("fulltextWindow", verseText);

				documentDescs.add(documentDesc);

			} catch (IOException e) {
				LOG.error("Error reading line.");
			}
		}
		return jsonFactory.map(results, false);
	}

	private Map<String, Object> documentDescMap(Document document) {
		final Map<String, Object> documentDesc = Maps.newHashMap();
		documentDesc.put("id", document.node.getId());
		documentDesc.put("callnumber", document.getMetadata("callnumber"));
		documentDesc.put("idnos", document.allIdnos());
		documentDesc.put("source", document.getSource().toString());
		return documentDesc;
	}

	private List<LayerNode<JsonNode>> fulltextQuery(String term) {
		return Lists.newArrayList(Iterables.limit(verseManager.fulltextQuery(term), 15));
	}

	private List<Document> idnoQuery(String term) {
		return Lists.newArrayList(Iterables.limit(Document.find(db, term), 10));
	}

	public static String toQuery(String search) {
		return new StringBuilder("*").append(search.replaceAll("\\*", "").toLowerCase()).append("*").toString();
	}

	private static SortedSet<String> toSortedValues(String[] metadata) {
		return Sets.newTreeSet(Arrays.asList(Objects.firstNonNull(metadata, new String[0])));
	}	
	
}
