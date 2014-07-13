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

import de.faustedition.VerseInterval;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.NodeWrapper;
import eu.interedition.text.Layer;
import eu.interedition.text.neo4j.LayerNode;
import eu.interedition.text.neo4j.Neo4jTextRepository;
import org.codehaus.jackson.JsonNode;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


public class GraphVerseInterval extends NodeWrapper implements VerseInterval {

	public static final String PREFIX = FaustGraph.PREFIX + ".verse-interval";
	public static final String NAME_KEY = PREFIX + ".name";
	public static final String START_KEY = PREFIX + ".start";
	public static final String END_KEY = PREFIX + ".end";

	public static final FaustRelationshipType VERSE_INTERVAL_IN_TRANSCRIPT_RT =
			new FaustRelationshipType("verse-interval-in-transcript");


	private static final Logger LOG = LoggerFactory.getLogger(GraphVerseInterval.class);


	public GraphVerseInterval(GraphDatabaseService db, int start, int end) {
		super(db.createNode());
		setStart(start);
		setEnd(end);
		LOG.debug("created vi: " + this);
	}

	public GraphVerseInterval(Node node) {
		super(node);
	}

	public String getName() {
		return (String) node.getProperty(NAME_KEY);
	}

	@Override
	public void setName(String name) {
		node.setProperty(NAME_KEY, name);
	}

	public int getStart() {
		return (Integer)node.getProperty(START_KEY);
	}

	public void setStart(int start) {
		node.setProperty(START_KEY, start);
	}

	public int getEnd() {
		return (Integer)node.getProperty(END_KEY);
	}

	public void setEnd(int end) {
		node.setProperty(END_KEY, end);
	}

	public void setTranscript(Layer<JsonNode> transcript) {
		this.node.createRelationshipTo(((LayerNode)transcript).node, VERSE_INTERVAL_IN_TRANSCRIPT_RT);
	}

	public LayerNode<JsonNode> getTranscript(Neo4jTextRepository textRepo) {
		Node transcriptNode = this.node.getSingleRelationship(VERSE_INTERVAL_IN_TRANSCRIPT_RT, Direction.OUTGOING).getEndNode();
		return new LayerNode<JsonNode>(textRepo, transcriptNode);
	}

	@Override
	public boolean overlapsWith(VerseInterval other) {
		return (getStart() < other.getEnd()) && (getEnd() > other.getStart());
	}

	@Override
	public String toString() {
		return "[" + getStart() + "-" + getEnd() + "]";
	}
}
