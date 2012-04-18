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
