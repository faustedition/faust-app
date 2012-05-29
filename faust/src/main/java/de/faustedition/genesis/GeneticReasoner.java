package de.faustedition.genesis;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.document.Document;
import de.faustedition.document.MaterialUnit;
import de.faustedition.graph.FaustGraph;
import de.faustedition.reasoning.GeneticReasoning;
import de.faustedition.reasoning.Inscription;
import de.faustedition.reasoning.RelationPrinter;
import de.faustedition.transcript.Transcript;
import de.faustedition.xml.XMLStorage;

@Component
public class GeneticReasoner extends Runtime implements Runnable {

	@Autowired
	private XMLStorage xml;

	@Autowired
	private FaustGraph graph;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private TransactionTemplate tt;

	@Autowired
	Environment environment;

	public static void main(String[] args) throws Exception {
		try {
			main(GeneticReasoner.class, args);
		} finally {
			System.exit(0);
		}
	}

	private static final Logger LOG = LoggerFactory
			.getLogger(GeneticReasoner.class);

	@Override
	public void run() {

		final Set<Inscription> inscriptions = inscriptions();

		LOG.debug("Reasoning on " + inscriptions.size() + " inscriptions");

		final GeneticReasoning reasoning = new GeneticReasoning(inscriptions);
		reasoning.initSyn();
		reasoning.initCon();
		reasoning.ruleSynImpliesPre();
		reasoning.ruleConImpliesPre();

		printGraph(reasoning, inscriptions);

	}

	private void printGraph(GeneticReasoning reasoning, Set<Inscription> inscriptions) {
		String path = environment.getRequiredProperty("reasoner.out");
		FileOutputStream out;
		PrintStream ps;
		try {
			out = new FileOutputStream(path);
			ps = new PrintStream(out);			
			RelationPrinter.startDot("genetic_graph", ps);
			//RelationPrinter.printRelationDot(reasoning.syn, "syn", "red", inscriptions, ps);
			//RelationPrinter.printRelationDot(reasoning.con, "con", "blue", inscriptions, ps);
			RelationPrinter.printRelationDot(reasoning.pre, "pre", "black", inscriptions, ps);
			RelationPrinter.endDot(ps);
			
			ps.close();
		} catch (FileNotFoundException e) {
			LOG.error("Error writing graph file", e);
		} finally {

		}
	}

	private HashSet<Inscription> inscriptions() {
		final HashSet<Inscription> inscriptions = new HashSet<Inscription>();

		try {
			for (MaterialUnit mu : graph.getMaterialUnits()) {
				if (mu instanceof Document) {
					final Document document = (Document) mu;
					final FaustURI source = document.getTranscriptSource();
					if (source != null) {
						Transcript transcript = tt
								.execute(new TransactionCallback<Transcript>() {
									@Override
									public Transcript doInTransaction(
											TransactionStatus status) {
										Session session = sessionFactory
												.getCurrentSession();
										try {
											Transcript transcript = Transcript
													.find(session, xml, source);
											TranscribedVerseInterval.register(
													session, transcript);
											Iterable<TranscribedVerseInterval> intervals = TranscribedVerseInterval
													.registeredFor(session,
															transcript);
											String name = document
													.getMetadataValue("wa-id");
											name = (name == null || ""
													.equals(name)) ? document
													.getMetadataValue("callnumber")
													: name;
											name = (name == null || ""
													.equals(name)) ? "noname"
													: name;
											Inscription inscription = new Inscription(
													name);

											for (TranscribedVerseInterval interval : intervals) {

												// filter: only 5th act
												int start = Math.max(
														interval.getStart(),
														11043);
												int end = Math.min(
														interval.getEnd(),
														12112);

												if (start <= end)
													inscription.addInterval(
															start, end);
											}
											if (inscription.size() > 0)
												inscriptions.add(inscription);
										} catch (IOException e) {
											LOG.error("IO error in " + source,
													e);
										} catch (XMLStreamException e) {
											LOG.error("XML error in " + source,
													e);
										}

										return null;
									}
								});
					}
				}

			}
		} catch (Exception e) {
			System.out.println(e);
		}
		return inscriptions;
	}
}
