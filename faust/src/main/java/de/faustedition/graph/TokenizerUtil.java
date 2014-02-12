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

package de.faustedition.graph;

import java.util.LinkedList;
import java.util.List;

import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.MultiRootedTree;
import org.goddag4j.Text;
import org.goddag4j.token.AbstractTokenMarkupGenerator;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

public class TokenizerUtil {

	public static void tokenize(MultiRootedTree trees, Element textRoot, AbstractTokenMarkupGenerator tokenGenerator,
			String tokenNs, String tokenRootName) {
		final GraphDatabaseService db = trees.getNode().getGraphDatabase();

		Element tokens = null;
		List<Text> textNodes = new LinkedList<Text>();
		Transaction tx = db.beginTx();
		try {

			tokens = new Element(db, tokenNs, tokenRootName);
			trees.addRoot(tokens);

			for (GoddagTreeNode node : textRoot.getDescendants(textRoot)) {
				if (node.getNodeType() == GoddagNode.NodeType.TEXT) {
					textNodes.add((Text) node);
				}
			}
			tx.success();
		} finally {
			tx.finish();
		}

		tokenGenerator.generate(textNodes, tokens);
	}

}
