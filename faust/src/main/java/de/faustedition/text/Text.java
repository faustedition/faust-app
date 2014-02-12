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

package de.faustedition.text;

import org.goddag4j.Element;
import org.goddag4j.MultiRootedTree;
import org.goddag4j.token.LineTokenMarkupGenerator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.NodeWrapper;
import de.faustedition.graph.TokenizerUtil;

public class Text extends NodeWrapper {
	public static final String PREFIX = FaustGraph.PREFIX + ".text";
	public static final String SOURCE_KEY = PREFIX + ".source";
	public static final String TITLE_KEY = PREFIX + ".title";
	private static final FaustRelationshipType MARKUP_VIEW_RT = new FaustRelationshipType("markup-view");

	private MultiRootedTree trees;

	public Text(Node node) {
		super(node);
		this.trees = new MultiRootedTree(node, MARKUP_VIEW_RT);
	}

	public Text(GraphDatabaseService db, FaustURI source, String title, Element root) {
		this(db.createNode());
		setSource(source);
		setTitle(title);
		getTrees().addRoot(root);
	}

	public MultiRootedTree getTrees() {
		return trees;
	}

	public void setSource(FaustURI uri) {
		node.setProperty(SOURCE_KEY, uri.toString());
	}

	public FaustURI getSource() {
		return FaustURI.parse((String) node.getProperty(SOURCE_KEY));
	}

	public String getTitle() {
		return (String) node.getProperty(TITLE_KEY);
	}

	public void setTitle(String title) {
		node.setProperty(TITLE_KEY, title);
	}

	public Element getDefaultRoot() {
		return getTrees().getRoot("tei", "text");
	}

	public void tokenize() {
		TokenizerUtil.tokenize(getTrees(), getDefaultRoot(), new LineTokenMarkupGenerator(), "f", "lines");
	}
}
