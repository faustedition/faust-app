package de.faustedition.transcript;

import de.faustedition.Runtime;
import de.faustedition.document.MaterialUnit;
import de.faustedition.graph.FaustGraph;
import de.faustedition.transcript.input.TranscriptInvalidException;
import de.faustedition.xml.XMLStorage;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.StopWatch;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Deque;

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
	private XMLStorage xml;

	@Autowired
	private Logger logger;
	
	@Autowired
	private TranscriptManager transcriptManager;

	@Override
	public void run() {
		logger.debug("Reading transcripts in the background");

		StopWatch stopWatch = new StopWatch();
		stopWatch.start();

		final Deque<MaterialUnit> queue = new ArrayDeque<MaterialUnit>(graph.getMaterialUnits());
		while (!queue.isEmpty()) {
			final MaterialUnit mu = queue.pop();

			queue.addAll(mu);

			if (mu.getTranscriptSource() == null) {
				continue;
			}
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						logger.debug("Reading transcript of {}: {}", mu, mu.getMetadataValue("source"));
            			transcriptManager.find(mu);
					} catch (IOException e) {
						if (logger.isWarnEnabled()) {
							logger.warn("I/O error while reading transcript from " + mu + ": "
                                    + mu.getMetadataValue("source"), e);
						}
					} catch (XMLStreamException e) {
						if (logger.isWarnEnabled()) {
							logger.warn("XML error while reading transcript from " + mu + ": "
                                    + mu.getMetadataValue("source"), e);
						}
					} catch (TranscriptInvalidException e) {
						if (logger.isWarnEnabled()) {
							logger.warn("Validation error while reading transcript from " + mu + ": "
                                    + mu.getMetadataValue("source"), e);
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
