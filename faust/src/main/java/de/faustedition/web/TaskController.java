package de.faustedition.web;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.TaskExecutor;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import de.faustedition.document.TranscriptionDocumentGenerator;
import de.faustedition.metadata.EncodingStatusManager;
import de.faustedition.metadata.IdentifierManager;
import de.faustedition.tei.EncodedTextDocumentBuilder;
import de.faustedition.tei.EncodedTextDocumentValidator;

@Controller
@RequestMapping("/task/")
public class TaskController {

	@Autowired
	private TaskExecutor taskExecutor;

	@Autowired
	private EncodedTextDocumentValidator documentValidator;

	@Autowired
	private EncodedTextDocumentBuilder documentBuilder;

	@Autowired
	private TranscriptionDocumentGenerator transcriptionDocumentGenerator;
	
	@Autowired
	private EncodingStatusManager encodingStatusManager;

	@Autowired
	private IdentifierManager identifierManager;

	@RequestMapping(value = "start/tei/validate", method = RequestMethod.POST)
	public void teiValidate() {
		taskExecutor.execute(documentValidator);
	}

	@RequestMapping(value = "start/tei/build", method = RequestMethod.POST)
	public void teiTemplate() {
		taskExecutor.execute(documentBuilder);
	}

	@RequestMapping(value = "start/document/transcriptions", method = RequestMethod.POST)
	public void documentTranscriptions() {
		taskExecutor.execute(transcriptionDocumentGenerator);
	}

	public void runEncodingStatusUpdate() {
		encodingStatusManager.update();
	}

	public void runIdentifierUpdate() {
		identifierManager.update();
	}
}
