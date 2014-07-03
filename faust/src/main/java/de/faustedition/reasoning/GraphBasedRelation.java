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

package de.faustedition.reasoning;

import de.faustedition.FaustURI;
import de.faustedition.document.Document;
import edu.bath.transitivityutils.ImmutableRelation;
import org.neo4j.graphdb.Node;

import java.util.Map;

public class GraphBasedRelation<E> implements ImmutableRelation<E> {

	private final Map<E, Node> nodeMap;
	
	private FaustURI geneticSource = null;
	
	public GraphBasedRelation(Map<E, Node> nodeMap) {
		this(nodeMap, null);
	}

	public GraphBasedRelation(Map<E, Node> nodeMap, FaustURI geneticSource) {
		this.nodeMap = nodeMap;
		this.geneticSource = geneticSource;
	}

	@Override
	public boolean areRelated(E subject, E object) {
			Document subjectDoc = (Document)(Document.forNode(nodeMap.get(subject)));
			Document objectDoc = (Document)(Document.forNode(nodeMap.get(object)));
				
		//TODO efficiency (this is cubic)
			return subjectDoc.geneticallyRelatedTo(geneticSource).contains(objectDoc);
	}
	
}
