package de.faustedition.transcript;

import com.google.common.collect.Sets;
import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.document.DocumentDescriptorHandler;
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
import java.util.Set;

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

		final Set<FaustURI> imported = Sets.<FaustURI>newHashSet();

		final Deque<MaterialUnit> queue = new ArrayDeque<MaterialUnit>(graph.getMaterialUnits());
		while (!queue.isEmpty()) {
			final MaterialUnit mu = queue.pop();
			final String source = mu.getMetadataValue(DocumentDescriptorHandler.internalKeyDocumentSource);
			final FaustURI transcriptSource = mu.getTranscriptSource();
			for (MaterialUnit child: mu) {
				if (!imported.contains(transcriptSource)) {
					imported.add(transcriptSource);
					queue.add(child);
				}
			//queue.addAll(mu);
			}
			if (mu.getTranscriptSource() == null || DocumentDescriptorHandler.noneURI.equals(transcriptSource)) {
				continue;
			}
			transactionTemplate.execute(new TransactionCallbackWithoutResult() {
				@Override
				protected void doInTransactionWithoutResult(TransactionStatus status) {
					try {
						logger.debug("Reading transcript {} referenced in {}", transcriptSource, source);
						transcriptManager.find(mu);
					} catch (IOException e) {
						if (logger.isWarnEnabled()) {
							logger.warn("I/O error while reading transcript from " + mu + ": "
                                    + source, e);
						}
					} catch (XMLStreamException e) {
						if (logger.isWarnEnabled()) {
							logger.warn("XML error while reading transcript from " + mu + ": "
                                    + source, e);
						}
					} catch (TranscriptInvalidException e) {
						if (logger.isWarnEnabled()) {
							logger.warn("Validation error while reading transcript from " + mu + ": "
                                    + source, e);
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
