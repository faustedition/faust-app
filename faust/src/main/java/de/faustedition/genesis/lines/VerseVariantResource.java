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

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;
import de.faustedition.FaustURI;
import de.faustedition.JsonRepresentationFactory;
import de.faustedition.document.Document;
import de.faustedition.document.MaterialUnit;
import de.faustedition.search.Normalization;
import de.faustedition.transcript.TranscriptManager;
import eu.interedition.text.Anchor;
import eu.interedition.text.neo4j.LayerNode;
import eu.interedition.text.neo4j.Neo4jTextRepository;
import org.codehaus.jackson.JsonNode;
import org.json.JSONArray;
import org.json.JSONException;
import org.neo4j.graphdb.GraphDatabaseService;
import org.restlet.ext.json.JsonRepresentation;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.Post;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class VerseVariantResource extends ServerResource {

	@Autowired
	private GraphDatabaseService graphDb;

	@Autowired
	Neo4jTextRepository textRepository;

	@Autowired
	private VerseManager verseManager;

	@Autowired
	TranscriptManager transcriptManager;

	@Autowired
	private JsonRepresentationFactory jsonRepresentationFactory;
	private ImmutableMap<MaterialUnit,Collection<GraphVerseInterval>> verseStatistics;
	private int from;
	private int to;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
	}

	private Set<Map<String, String>> variantsFor (int lineNum) {
		HashSet<Map<String, String>> documentDescriptions = Sets.newHashSet();

		Iterable<GraphVerseInterval> intervals = verseManager.forInterval(new SimpleVerseInterval("---", lineNum, lineNum));
		for (GraphVerseInterval interval : intervals) {
			Map<String, String> documentDescription = Maps.newHashMap() ;
			LayerNode<JsonNode> transcript = interval.getTranscript(textRepository);
			Document document = (Document)(transcriptManager.materialUnitForTranscript(transcript));

			documentDescription.put("name", document.toString());
			documentDescription.put("source", document.getSource().toString());
			documentDescription.put("variantText", "Test line, text to be inserted here.");

			documentDescriptions.add(documentDescription);
/*
			textRepo.query(and(text(transcript), name(new Name(TextConstants.TEI_NS, "l")

			Anchor<JsonNode> anchor = Iterables.getOnlyElement(verse.getAnchors());
			try {
				String verseText = anchor.getText().read(anchor.getRange());
				LOG.trace("Indexing verse: " + verseText);
				verseTextIndex.add(((LayerNode)verse).node, "fulltext",
						Normalization.normalize(verseText));

			} catch (IOException e) {
				LOG.error("Error indexing line " + lineNum);
			}
*/

		}
		return documentDescriptions;
	}

	@Post("json")
	public Representation verseVariants(JsonRepresentation requestData) throws JSONException {
		final Map<Integer, Set<Map<String, String>>> variantsForLines = Maps.newHashMap();


		JSONArray lineNumbers = requestData.getJsonArray();
		for (int i=0; i < lineNumbers.length(); i++) {
			Set<Map<String, String>> variantsForLine = variantsFor(lineNumbers.getInt(i));
			for (Map<String, String> variant: variantsForLine) {
				variantsForLine.add(variant);
			}
			variantsForLines.put(lineNumbers.getInt(i), variantsForLine);
		}
		return jsonRepresentationFactory.map(variantsForLines, false);
	}
}
