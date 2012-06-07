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
import de.faustedition.reasoning.FaustReasoning;
import de.faustedition.reasoning.ImmutableRelation;
import de.faustedition.reasoning.Inscription;
import de.faustedition.reasoning.RelationPrinter;
import de.faustedition.reasoning.Rule;
import de.faustedition.reasoning.Rules;
import de.faustedition.transcript.Transcript;
import de.faustedition.xml.XMLStorage;
import edu.bath.transitivityutils.Relation;

@Component
public class GeneticReasoner extends Runtime implements Runnable {

	private static int FROM_LINE = 11043;
	private static int TO_LINE = 12112;
	
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
		reason(inscriptions);
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
											if (name == null || "".equals(name))
												name = document
														.getMetadataValue("callnumber");
											if (name == null || "".equals(name)
													|| "-".equals(name)) {
												String path = document
														.getSource().getPath();
												String filename = path.substring(path
														.lastIndexOf("/") + 1);
												String filenameStripped = filename
														.replaceAll("\\.xml$",
																"");
												name = filenameStripped;
											}
											Inscription inscription = new Inscription(
													name);

											for (TranscribedVerseInterval interval : intervals) {

												// filter: only 5th act
												int start = Math.max(
														interval.getStart(),
														FROM_LINE);
												int end = Math.min(
														interval.getEnd(),
														TO_LINE);

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
	
	public void reason(Set<Inscription> inscriptions) {

		LOG.debug("Reasoning on " + inscriptions.size() + " inscriptions");

		final FaustReasoning reasoning = new FaustReasoning(inscriptions);
		
		reasoning.reason();

		String path = environment.getRequiredProperty("reasoner.out") + "/";

		try {
			RelationPrinter.printGraph(reasoning.pre, "pre", "black", 1, inscriptions,
					path + "pre.dot");

			RelationPrinter.printGraph(reasoning.econ, "econ", "black", 1,
					inscriptions, path + "econ.dot");

			RelationPrinter.printGraph(reasoning.pcon, "pcon", "black", 1,
					inscriptions, path + "pcon.dot");

			
			RelationPrinter.printGraph(reasoning.syn, "syn", "black", 1,
					inscriptions, path + "syn.dot");

			Relation synContradictingPre = reasoning.contradictions(
					reasoning.syn, reasoning.pre);

			RelationPrinter.printGraph(synContradictingPre, "syn", "red", 1,
					inscriptions, path + "syn_contradicting_pre.dot");

			RelationPrinter.printInscriptionCSV(RelationPrinter.orderUniverse(
					reasoning.pre, inscriptions), FROM_LINE, TO_LINE, path + "gantt.csv");

		} catch (FileNotFoundException e) {
			LOG.error("Error writing graph file", e);
		} finally {

		}

	}

}
