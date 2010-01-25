package de.faustedition.model.tei;

import javax.jcr.Repository;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import de.faustedition.model.report.ReportSender;

@Service
public class EncodedTextDocumentValidationTask {

	@Autowired
	private Repository repository;
	
	@Autowired
	private ReportSender reportSender;
	
	public void validateDocuments() {
	}
}
