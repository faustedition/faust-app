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

package de.faustedition.genesis.dating;

import au.com.bytecode.opencsv.CSVReader;
import de.faustedition.Runtime;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import org.joda.time.LocalDate;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.regex.Pattern;

@Component
public class GeneticRelationManager extends Runtime implements Runnable {
	private static final FaustRelationshipType GENETIC_REL = new FaustRelationshipType("is-genetically-related"); 

	@Autowired
	private FaustGraph graph;

	@Autowired
	private GraphDatabaseService db;

	@Autowired
	private Logger logger;

	@Override
	public void run() {
		feedGraph();
	}

    @Transactional
	public void feedGraph() {
		final Pattern numbers = Pattern.compile("\\d+");
		Transaction tx = db.beginTx();
		/* //TODO change to new range model
		try {
			final SortedMap<Integer, Element> textLineIndex = textLineIndex();
			for (GoddagTranscript t : graph.getTranscripts()) {
				if (t.getType() != TranscriptType.TEXTUAL || !t.getSource().isTextEncodingDocument()) {
					continue;
				}

				logger.debug(t.toString());
				final Element root = t.getDefaultRoot();


				new GoddagVisitor() {
					public void startElement(Element root, Element element) {
						if ("tei:l".equals(element.getQName())) {
							String lineNumbers = element.getAttributeValue("tei", "n");
							if (lineNumbers != null) {
								Matcher lnMatcher = numbers.matcher(lineNumbers);
								while (lnMatcher.find()) {
									final Integer lineNumber = Integer.valueOf(lnMatcher.group());
									final Element target = textLineIndex.get(lineNumber);
									if (target != null) {
										element.node.createRelationshipTo(target.node, GENETIC_REL);

									}
								}
							}
						}
					};
				}.visit(root, root);


			}
			tx.success();
		} finally {
			tx.finish();
		}
        */
	}

/*  //TODO change to new text model
	protected SortedMap<Integer, Element> textLineIndex() {
		final SortedMap<Integer, Element> index = new TreeMap<Integer, Element>();
		Transaction tx = db.beginTx();
		try {
			SortedMap<FaustURI, Text> texts = new TreeMap<FaustURI, Text>();
			for (Text text : graph.getTexts()) {
				if (text.getSource().getPath().endsWith("Faust_0.xml")) {
					continue;
				}
				texts.put(text.getSource(), text);
			}
			for (Text text : texts.values()) {
				logger.debug(text.getSource().toString());
				final Element root = text.getDefaultRoot();
				new GoddagVisitor() {
					public void startElement(Element root, Element element) {
						if ("tei:l".equals(element.getQName())) {
							final String lnStr = element.getAttributeValue("tei", "n");
							if (lnStr != null) {
								final Integer ln = Integer.valueOf(lnStr);
								if (!index.containsKey(ln)) {
									index.put(ln, element);
								}
							}
						}
					};
				}.visit(root, root);
			}
			tx.success();
		} finally {
			tx.finish();
		}
		return index;
	}
*/

	protected void manuscriptDating() throws IOException {
		final InputStream sourceStream = getClass().getResourceAsStream("manuscript-dating.csv");
		final CSVReader reader = new CSVReader(new InputStreamReader(sourceStream, "UTF-8"), ',', '"', '\\');
		String[] row = reader.readNext();
		while ((row = reader.readNext()) != null) {
			final String cn = row[1].trim();
			if (cn.isEmpty()) {
				continue;
			}

			final LocalDate start = parseDate(row[10].trim(), row[11].trim(), row[12].trim());
			final LocalDate end = parseDate(row[13].trim(), row[14].trim(), row[15].trim());
			if (start == null && end == null) {
				continue;
			}

			System.out.printf("%s ==> %s -- %s\n", cn, start, end);
		}
	}

	private LocalDate parseDate(String dayStr, String monthStr, String yearStr) {
		if (yearStr.isEmpty()) {
			return null;
		}
		int year = Integer.parseInt(yearStr);

		int month = 1;
		if (!monthStr.isEmpty() && !monthStr.matches("^0+$")) {
			month = Integer.parseInt(monthStr);
		}

		int day = 1;
		if (!dayStr.isEmpty() && !dayStr.matches("^0+$")) {
			day = Integer.parseInt(dayStr);
		}

		return new LocalDate(year, month, day);
	}

	public static void main(String[] args) {
		try {
			main(GeneticRelationManager.class, args);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			System.exit(0);
		}
	}

/*
	//TODO change to new text model
	public Set<Document> findRelatedDocuments(Element l) {
		Set<Document> related = new HashSet<Document>();
		for (Relationship r : l.node.getRelationships(GENETIC_REL)) {
			final GoddagTranscript transcript = GoddagTranscript.find((GoddagTreeNode) GoddagNode.wrap(r.getOtherNode(l.node)));
			if (transcript == null) {
				continue;
			}
			
			MaterialUnit transcribed = MaterialUnit.find(transcript);
			MaterialUnit parent = transcribed.getParent();
			while (parent != null) {
				transcribed = parent;
				parent = parent.getParent();
			}
			
			if (transcribed instanceof Document) {
				related.add((Document) transcribed);
			}
		}
		return related;
	}
*/
}
