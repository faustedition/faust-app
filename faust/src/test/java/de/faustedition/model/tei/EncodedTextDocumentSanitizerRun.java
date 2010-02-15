package de.faustedition.model.tei;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;
import de.faustedition.model.report.EncodedTextDocumentSanitizer;

public class EncodedTextDocumentSanitizerRun extends AbstractModelContextTest {

	@Autowired
	private EncodedTextDocumentSanitizer sanitizer;

	@Test
	public void runSanitizer() {
		sanitizer.sanitize();
	}
}
