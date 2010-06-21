package de.faustedition.tei;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;

public class EncodedTextDocumentTasks extends AbstractContextTest {

	@Autowired
	EncodedTextDocumentValidator validator;

	@Autowired
	EncodedTextDocumentBuilder builder;
	
	@Autowired
	EncodedTextDocumentSanitizer sanitizer;
	
	@Test
	public void runValidator() {
		validator.run();
	}
	
	public void runBuilder() {
		builder.run();
	}

	public void runSanitizer() {
		sanitizer.run();
	}

}
