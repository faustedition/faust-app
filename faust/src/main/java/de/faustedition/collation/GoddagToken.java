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
