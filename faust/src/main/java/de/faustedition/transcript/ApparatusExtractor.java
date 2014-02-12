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

import static de.faustedition.xml.Namespaces.TEI_NS_PREFIX;

import java.util.ArrayList;
import java.util.List;

import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.visit.GoddagVisitor;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.google.common.collect.Lists;

import de.faustedition.xml.Namespaces;

public class ApparatusExtractor {

	public void extract(GoddagTranscript transcript) {
		final GraphDatabaseService db = transcript.node.getGraphDatabase();
		final Transaction tx = db.beginTx();
		try {
			final Element source = transcript.getDefaultRoot();
			final Element apps = transcript.getTrees().getRoot(Namespaces.FAUST_NS_PREFIX, "apps");
			final List<Element> appElements = new ArrayList<Element>();
			new GoddagVisitor() {

				public void startElement(Element root, Element element) {
					if ("app".equals(element.getName()) && TEI_NS_PREFIX.equals(element.getPrefix())) {
						appElements.add(element);
					}
				};

			}.visit(source, source);

			for (Element app : appElements) {
				app.copy(source, apps);
				apps.insert(apps, app, null);

				for (GoddagNode appChild : Lists.newArrayList(app.getChildren(source))) {
					if (appChild.getNodeType() != GoddagNode.NodeType.ELEMENT) {
						continue;
					}
					final Element rdg = (Element) appChild;
					if ("lem".equals(rdg.getName()) && TEI_NS_PREFIX.equals(rdg.getPrefix())) {
						app.merge(source, rdg);
					} else {
						rdg.getParent(source).remove(source, rdg, true);
					}
				}
			}
			tx.success();
		} finally {
			tx.finish();
		}

	}
}
