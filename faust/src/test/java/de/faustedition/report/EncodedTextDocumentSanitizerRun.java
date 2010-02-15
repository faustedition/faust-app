package de.faustedition.report;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractModelContextTest;
import de.faustedition.report.EncodedTextDocumentSanitizer;

public class EncodedTextDocumentSanitizerRun extends AbstractModelContextTest {

	@Autowired
	private EncodedTextDocumentSanitizer sanitizer;

	@Test
	public void runSanitizer() {
		sanitizer.sanitize();
	}
}
