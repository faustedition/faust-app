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

package de.faustedition.genesis;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import de.faustedition.FaustURI;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.NodeWrapper;

public class GeneticSource extends NodeWrapper {

	public static final String PREFIX = FaustGraph.PREFIX + ".geneticSource";
	public static final String SOURCE_KEY = PREFIX + ".source";
	public static final String URI_KEY = PREFIX + ".uri";

	public GeneticSource(Node node) {
		super(node);
	}

	public GeneticSource(GraphDatabaseService db, FaustURI uri) {
		this(db.createNode());
		setUri(uri);
	}

	public void setUri(FaustURI uri) {
		node.setProperty(URI_KEY, uri.toString());
	}

	public FaustURI getUri() {
		return FaustURI.parse((String) node.getProperty(URI_KEY));
	}
}
