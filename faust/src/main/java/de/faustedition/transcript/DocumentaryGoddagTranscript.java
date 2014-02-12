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

import static de.faustedition.xml.Namespaces.FAUST_NS_PREFIX;
import static de.faustedition.xml.Namespaces.TEI_NS_PREFIX;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.goddag4j.Element;
import org.goddag4j.Text;
import org.goddag4j.visit.GoddagVisitor;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.Transaction;

import de.faustedition.FaustURI;
import de.faustedition.xml.Namespaces;

public class DocumentaryGoddagTranscript extends GoddagTranscript {

	public DocumentaryGoddagTranscript(Node node) {
		super(node);
	}

	public DocumentaryGoddagTranscript(GraphDatabaseService db, FaustURI source, Element root, SortedSet<FaustURI> facsimileReferences) {
		super(db, TranscriptType.DOCUMENTARY, source, root);
		setFacsimileReferences(facsimileReferences);
	}

	public void setFacsimileReferences(SortedSet<FaustURI> facsimileReferences) {
		String[] uris = new String[facsimileReferences.size()];
		int uc = 0;
		for (FaustURI uri : facsimileReferences) {
			uris[uc++] = uri.toString();
		}
		node.setProperty(PREFIX + ".documentary.facsimiles", uris);
	}

	public SortedSet<FaustURI> getFacsimileReferences() {
		SortedSet<FaustURI> facsimileReferences = new TreeSet<FaustURI>();
		for (String uri : ((String[]) node.getProperty(PREFIX + ".documentary.facsimiles"))) {
			facsimileReferences.add(FaustURI.parse(uri));
		}
		return facsimileReferences;
	}

	@Override
	public Element getDefaultRoot() {
		return getTrees().getRoot(Namespaces.TEI_SIG_GE_PREFIX, "document");
	}

	public void postprocess() {
		final GraphDatabaseService db = node.getGraphDatabase();
		final Transaction tx = db.beginTx();
		try {
			final Element root = getDefaultRoot();
			new GoddagVisitor() {

				private Element hand = null;
				private String handId = null;
				private Element hands = getTrees().getRoot(FAUST_NS_PREFIX, "hands");
				private Set<Element> handShifts = new HashSet<Element>();

				@Override
				public void text(Element root, Text text) {
					if (hand != null) {
						hand.insert(hands, text, null);
					}
				}

				@Override
				public void startElement(Element root, Element element) {
					if ("handShift".equals(element.getName()) && TEI_NS_PREFIX.equals(element.getPrefix())) {
						handShifts.add(element);
						String newHandId = element.getAttributeValue(TEI_NS_PREFIX, "new");
						if (newHandId != null) {
							newHandId = newHandId.replaceAll("^#", "");
							if (handId == null || !newHandId.equals(handId)) {
								insertHand();
								handId = newHandId;
								hand = new Element(db, FAUST_NS_PREFIX, "hand");
								hand.setAttribute(FAUST_NS_PREFIX, "id", handId);
							}
						}
					}
				}

				@Override
				public void endElement(Element root, Element element) {
					if (element.equals(root)) {
						insertHand();
						for (Element handShift : handShifts) {
							handShift.getParent(root).remove(root, handShift, true);
						}
					}
				}

				protected void insertHand() {
					if (hand != null && hand.hasChildren(hands)) {
						hands.insert(hands, hand, null);
					}
				}

			}.visit(root, root);
			tx.success();
		} finally {
			tx.finish();
		}
	}
}
