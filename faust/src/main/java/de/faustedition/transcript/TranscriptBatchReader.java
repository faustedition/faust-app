package de.faustedition.transcript;

import java.io.IOException;

import javax.xml.stream.XMLStreamException;

import org.hibernate.SessionFactory;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

import de.faustedition.Runtime;
import de.faustedition.document.Document;
import de.faustedition.document.MaterialUnit;
import de.faustedition.graph.FaustGraph;
import de.faustedition.transcript.input.TranscriptInvalidException;
import de.faustedition.xml.XMLStorage;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service
public class TranscriptBatchReader extends Runtime implements Runnable {

	@Autowired
	private FaustGraph graph;

	@Autowired
	private TransactionTemplate transactionTemplate;

	@Autowired
	private SessionFactory sessionFactory;

	@Autowired
	private XMLStorage xml;

	@Autowired
	private Logger logger;
	
	@Autowired
	private DocumentaryTranscripts documentaryTranscripts;
	
	@Autowired
	private TextualTranscripts textualTranscripts;

	@Override
	public void run() {
		logger.debug("Reading transcripts in the background");

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();
		for (final MaterialUnit mu : graph.getMaterialUnits()) {
//			if ((mu instanceof Document)) {
//				continue;
//			}
			if (mu.getTranscriptSource() == null) {
				continue;
			}
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						logger.debug("Reading transcript of {}", mu);
						if (mu instanceof Document) 
							textualTranscripts.read(sessionFactory.getCurrentSession(), xml, mu);
						else
							documentaryTranscripts.read(sessionFactory.getCurrentSession(), xml, mu);
						
					} catch (IOException e) {
						if (logger.isWarnEnabled()) {
							logger.warn("I/O error while reading transcript from " + mu, e);
						}
					} catch (XMLStreamException e) {
						if (logger.isWarnEnabled()) {
							logger.warn("XML error while reading transcript from " + mu, e);
						}
					} catch (TranscriptInvalidException e) {
						if (logger.isWarnEnabled()) {
							logger.warn("Validation error while reading transcript from " + mu, e);
						}						
					}
				}
			});
		}
		stopWatch.stop();

		logger.debug("Read transcripts in the background: {} s", stopWatch.getTotalTimeSeconds());
	}

	public static void main(String... args) throws Exception {
		main(TranscriptBatchReader.class, args);
		System.exit(0);
	}
}
