package de.faustedition.genesis;

import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

import javax.xml.stream.XMLStreamException;

import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallback;
import org.springframework.transaction.support.TransactionTemplate;

import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.dataimport.DataImport;
import de.faustedition.document.Document;
import de.faustedition.document.MaterialUnit;
import de.faustedition.graph.FaustGraph;
import de.faustedition.reasoning.GeneticReasoning;
import de.faustedition.reasoning.Inscription;
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
		final GeneticReasoning reasoning = new GeneticReasoning();
		LOG.debug("Reasoning on " + inscriptions.size() + " inscriptions");
		reasoning.reason(inscriptions);

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
									String name = document.getMetadataValue("callnumber");
									name = (name == null || "".equals(name)) ? "noname" : name;
									Inscription inscription = new Inscription(name);
									for (TranscribedVerseInterval interval : intervals) {
										inscription.addInterval(interval.getStart(), interval.getEnd());									
									}
									if (inscription.size() > 0)
										inscriptions.add(inscription);
								} catch (IOException e) {
									LOG.error("IO error in " + source, e);
								} catch (XMLStreamException e) {
									LOG.error("XML error in " + source, e);
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
