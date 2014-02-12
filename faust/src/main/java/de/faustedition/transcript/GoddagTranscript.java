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

import org.goddag4j.Element;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.MultiRootedTree;
import org.goddag4j.token.WhitespaceTokenMarkupGenerator;
import org.neo4j.graphdb.Direction;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Relationship;

import com.google.common.base.Objects;
import com.google.common.collect.Iterables;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.NodeWrapper;
import de.faustedition.graph.TokenizerUtil;

public abstract class GoddagTranscript extends NodeWrapper {

	public static final FaustRelationshipType TRANSCRIPT_RT = new FaustRelationshipType("transcribes");
	public static final String PREFIX = FaustGraph.PREFIX + ".transcript";
	public static final String SOURCE_KEY = PREFIX + ".source";

	private static final FaustRelationshipType MARKUP_VIEW_RT = new FaustRelationshipType("markup-view");

	private final MultiRootedTree trees;

	protected GoddagTranscript(Node node) {
		super(node);
		this.trees = new MultiRootedTree(node, MARKUP_VIEW_RT);
	}

	protected GoddagTranscript(GraphDatabaseService db, TranscriptType type, FaustURI source, Element root) {
		this(db.createNode());
		setType(type);
		setSource(source);
		this.trees.addRoot(root);
	}

	public MultiRootedTree getTrees() {
		return trees;
	}

	public void setType(TranscriptType type) {
		node.setProperty(PREFIX + ".type", type.name().toLowerCase());
	}

	public TranscriptType getType() {
		return getType(node);
	}

	public static GoddagTranscript forNode(Node node) {
		TranscriptType type = getType(node);
		switch (type) {
		case DOCUMENTARY:
			return new DocumentaryGoddagTranscript(node);
		case TEXTUAL:
			return new TextualGoddagTranscript(node);
		}
		throw new IllegalArgumentException(type.toString());
	}

	public static TranscriptType getType(Node node) {
		return TranscriptType.valueOf(((String) node.getProperty(PREFIX + ".type")).toUpperCase());
	}

	public void setSource(FaustURI uri) {
		node.setProperty(SOURCE_KEY, uri.toString());
	}

	public FaustURI getSource() {
		return FaustURI.parse((String) node.getProperty(SOURCE_KEY));
	}

	public abstract Element getDefaultRoot();

	public abstract void postprocess();

	public void tokenize() {
		TokenizerUtil.tokenize(getTrees(), getDefaultRoot(), new WhitespaceTokenMarkupGenerator(), "f", "words");
	}

	@Override
	public String toString() {
		return Objects.toStringHelper(this).add("type", getType()).add("source", getSource()).toString();
	}

	public static GoddagTranscript find(GoddagTreeNode node) {
		Element root = Iterables.getFirst(node.getRoots(), null);
		if (root == null) {
			return null;
		}
		for (Relationship r : root.node.getRelationships(MARKUP_VIEW_RT, Direction.INCOMING)) {
			return forNode(r.getStartNode());
		}
		return null;

	}
}
