package de.faustedition.genesis;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashSet;
import java.util.Set;
import java.util.SortedMap;
import java.util.TreeMap;
import java.util.logging.Logger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.goddag4j.Element;
import org.goddag4j.GoddagNode;
import org.goddag4j.GoddagTreeNode;
import org.goddag4j.visit.GoddagVisitor;
import org.joda.time.LocalDate;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.neo4j.graphdb.Transaction;

import au.com.bytecode.opencsv.CSVReader;

import com.google.inject.Inject;
import com.google.inject.Singleton;

import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.document.Document;
import de.faustedition.document.MaterialUnit;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.text.Text;
import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.Transcript.Type;

@Singleton
public class GeneticRelationManager extends Runtime implements Runnable {
	private static final FaustRelationshipType GENETIC_REL = new FaustRelationshipType("is-genetically-related"); 
	private final FaustGraph graph;
	private final GraphDatabaseService db;
	private final Logger logger;

	@Inject
	public GeneticRelationManager(FaustGraph graph, GraphDatabaseService db, Logger logger) {
		this.graph = graph;
		this.db = db;
		this.logger = logger;
	}

	@Override
	public void run() {
		final Pattern numbers = Pattern.compile("\\d+");
		Transaction tx = db.beginTx();
		try {
			final SortedMap<Integer, Element> textLineIndex = textLineIndex();
			for (Transcript t : graph.getTranscripts()) {
				if (t.getType() != Type.TEXTUAL || !t.getSource().isTextEncodingDocument()) {
					continue;
				}

				logger.info(t.toString());
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

	}

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
				logger.info(text.getSource().toString());
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

	public Set<Document> findRelatedDocuments(Element l) {
		Set<Document> related = new HashSet<Document>();
		for (Relationship r : l.node.getRelationships(GENETIC_REL)) {
			final Transcript transcript = Transcript.find((GoddagTreeNode) GoddagNode.wrap(r.getOtherNode(l.node)));
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
}
