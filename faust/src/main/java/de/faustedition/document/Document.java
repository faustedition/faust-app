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

package de.faustedition.document;

import com.google.common.base.*;
import com.google.common.base.Objects;
import com.google.common.collect.Iterables;
import de.faustedition.FaustURI;
import de.faustedition.genesis.dating.MacrogeneticRelationManager;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.NodeWrapper;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.WildcardQuery;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.RelationshipType;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.graphdb.index.IndexManager;

import javax.annotation.Nullable;
import java.net.URI;
import java.util.*;
import java.util.regex.Pattern;

import static org.neo4j.graphdb.Direction.OUTGOING;

public class Document extends MaterialUnit {
	private static final String PREFIX = FaustGraph.PREFIX + ".document";

	private static final String SOURCE_KEY = PREFIX + ".source";
	private static final String URI_KEY = PREFIX + "uri";
	private static final String URI_PART_KEY = PREFIX + "uri-part";	
	private static final String CALLNUMBER_KEY = METADATA_PREFIX + "callnumber";
	private static final String ALL_IDNOS_KEY = METADATA_PREFIX + "all-idnos";
	private static final String WA_ID_KEY = METADATA_PREFIX + "wa-id";

	private static final Pattern ALPHA_NUMERIC_PATTERN = Pattern.compile("[a-zA-Z0-9]");

	public static final String GENETIC_SOURCE_PROPERTY = "genetic-source";

	/* Indices */

	public static final  String INDEX_ID = "index-id";
	public static final String INDEX_SOURCE = "index-source";
	public static final String INDEX_URI = "index-uri";


	public Document(Node node) {
		super(node);
		// type check
		Preconditions.checkArgument(Type.ARCHIVALDOCUMENT.equals(this.getType()));
	}

	public Document(Node node, Type type, FaustURI source) {
		super(node, type);
		setSource(source);
	}

	public FaustURI getSource() {
		return FaustURI.parse((String) node.getProperty(SOURCE_KEY));
	}

	public void setSource(FaustURI uri) {
		node.setProperty(SOURCE_KEY, uri.toString());
	}

	/**
	 * @param geneticSource filter; can be null
	 */
	public Set<Document> geneticallyRelatedTo(FaustURI geneticSource /*, RelationshipType type*/) {
		RelationshipType type = MacrogeneticRelationManager.TEMP_PRE_REL;
		final Iterable<Relationship> relationships = node.getRelationships(type, OUTGOING);

		final Set<Document> result = new HashSet<Document>();
		
		for (Relationship relationship : relationships) {
			if (geneticSource != null && relationship.getProperty(GENETIC_SOURCE_PROPERTY).equals(geneticSource.toString())){
				final Document document = NodeWrapper.newInstance(Document.class, relationship.getEndNode());
				result.add(document);
			}
		}
			return result;
	}

	public static Document findBySource(GraphDatabaseService db, FaustURI source) {
		final Node node = db.index().forNodes(INDEX_SOURCE).get(SOURCE_KEY, source).getSingle();
		return (node == null ? null : new Document(node));
	}

	public static Document findByUri(GraphDatabaseService db, FaustURI uri) {
		try {
			final Node node = db.index().forNodes(INDEX_URI).get(URI_KEY, uri).getSingle();
			return (node == null ? null : new Document(node));
		} catch(NoSuchElementException e) {
			return null;
		}
	}


	public static Iterable<Document> find(GraphDatabaseService db, String id) {

		// query by idno
		final BooleanQuery idQuery = new BooleanQuery();
		idQuery.add(new WildcardQuery(new Term(ALL_IDNOS_KEY, id)), BooleanClause.Occur.SHOULD);
		idQuery.add(new WildcardQuery(new Term(WA_ID_KEY, id)), BooleanClause.Occur.SHOULD);
		idQuery.add(new WildcardQuery(new Term(URI_PART_KEY, id)), BooleanClause.Occur.SHOULD);
		IndexHits<Node> idResults = db.index().forNodes(INDEX_ID).query(idQuery);

		return Iterables.transform(idResults, newWrapperFunction(Document.class));
	}

    public Iterable<MaterialUnit> getPages() {
        Predicate<MaterialUnit> isPage = new Predicate<MaterialUnit>() {
            @Override
            public boolean apply(@Nullable MaterialUnit input) {
                return input != null && Type.PAGE.equals(input.getType());
            }
        };
        return Iterables.filter(getSortedContents(), isPage);
    }


	public void index() {
		final IndexManager indexManager = node.getGraphDatabase().index();

		indexManager.forNodes(INDEX_SOURCE).add(node, SOURCE_KEY, getSource());

		final Index<Node> idIndex = indexManager.forNodes(INDEX_ID);

		for (String uri: Objects.firstNonNull(getMetadata("uri"), new String[0])) {

				try {
					indexManager.forNodes(INDEX_URI).add(node, URI_KEY, new FaustURI(new URI(uri)));
				} catch (Exception e) {
					// TODO error logging
	                //logger.error("error!", e);
				}
				try {
					idIndex.add(node, URI_PART_KEY, uri.substring("faust://document/".length()).toLowerCase());
				} catch (IndexOutOfBoundsException e) {
					//do nothing
				}
				
		}

		String allIdnosToIndex = allIdnos();


		for (String callnumber : Objects.firstNonNull(getMetadata("callnumber"), new String[0])) {
			idIndex.add(node, ALL_IDNOS_KEY, allIdnosToIndex.toLowerCase());
		}
		for (String waId : Objects.firstNonNull(getMetadata("wa-id"), new String[0])) {
			if (ALPHA_NUMERIC_PATTERN.matcher(waId).find()) {
				idIndex.add(node, WA_ID_KEY, waId.toLowerCase());
			}
		}
	}

	public String allIdnos() {
		Iterable<String> idnoTypes = getMetadataKeys(CALLNUMBER_KEY + ".");

		Iterable<String> idnoValues = Iterables.transform(idnoTypes, new Function<String, String>() {
			@Override
			public String apply(@Nullable String input) {
				return getMetadataValue("callnumber" + "." + input);
			}
		});

		return Joiner.on("; ").join(idnoValues);
	}
}
