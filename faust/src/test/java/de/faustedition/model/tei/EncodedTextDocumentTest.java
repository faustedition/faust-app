package de.faustedition.model.tei;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;
import de.faustedition.model.xml.XmlUtil;

public class EncodedTextDocumentTest extends AbstractModelContextTest {

	@Autowired
	private EncodedTextDocumentManager documentManager;

	@Autowired
	private EncodedTextDocumentValidationTask validationTask;
	
	@Test
	public void enhanceNewDocument() {
		EncodedTextDocument d = documentManager.process(EncodedTextDocument.create());
		XmlUtil.serialize(d.getDom(), System.out, true);
	}
	
	@Test
	public void runValidatorTask() {
		validationTask.validate();
	}
}
