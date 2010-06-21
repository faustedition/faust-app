package de.faustedition.web;

import org.apache.commons.lang.time.StopWatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.faustedition.Log;
import de.faustedition.document.TranscriptionDocumentGenerator;
import de.faustedition.metadata.EncodingStatusManager;
import de.faustedition.metadata.IdentifierManager;
import de.faustedition.tei.EncodedTextDocumentBuilder;
import de.faustedition.tei.EncodedTextDocumentSanitizer;
import de.faustedition.tei.EncodedTextDocumentValidator;
import de.faustedition.tei.WhitespaceNormalizationReporter;

@Controller
@RequestMapping("/task/")
public class TaskController {

	@Autowired
	private TaskExecutor taskExecutor;

	@Autowired
	private EncodedTextDocumentBuilder documentBuilder;

	@Autowired
	private EncodedTextDocumentSanitizer documentSanitizer;

	@Autowired
	private EncodedTextDocumentValidator documentValidator;

	@Autowired
	private TranscriptionDocumentGenerator transcriptionDocumentGenerator;

	@Autowired
	private EncodingStatusManager encodingStatusManager;

	@Autowired
	private IdentifierManager identifierManager;
	
	@Autowired
	private WhitespaceNormalizationReporter whitespaceNormalizationReporter;
	

	@RequestMapping(value = "run", method = RequestMethod.POST)
	public void teiValidate() {
		taskExecutor.execute(new Runnable() {

			@Override
			public void run() {
				Log.LOGGER.info("Running periodic tasks");				
				StopWatch sw = new StopWatch();
				sw.start();
				
				transcriptionDocumentGenerator.run();
				documentBuilder.run();
				documentSanitizer.run();
				documentValidator.run();
				encodingStatusManager.run();
				identifierManager.run();
				whitespaceNormalizationReporter.run();
				
				sw.stop();
				Log.LOGGER.info("Periodic tasks finished in: " + sw);
			}
		});
	}
}
