package de.faustedition.collation;

import org.goddag4j.Element;
import org.goddag4j.GoddagTreeNode;

public class GoddagToken implements Token {

	final GoddagTreeNode node;
	final Element root;
	
	private String text;

	public GoddagToken(GoddagTreeNode node, Element root) {
		this.node = node;
		this.root = root;
	}

	@Override
	public String text() {
		if (text == null) {
			text = node.getText(root);
		}
		return text;
	}

}
