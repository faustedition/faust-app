package de.faustedition;

import java.io.IOException;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.metadata.EncodingStatusManager;
import de.faustedition.metadata.IdentifierManager;
import de.faustedition.tei.EncodedTextDocumentSanitizer;

public class PeriodicalTasks extends AbstractModelContextTest {

	@Autowired
	private EncodingStatusManager encodingStatusManager;

	@Autowired
	private IdentifierManager identifierManager;

	@Autowired
	private EncodedTextDocumentSanitizer sanitizer;

	public void runEncodingStatusUpdate() {
		encodingStatusManager.update();
	}

	public void runIdentifierUpdate() {
		identifierManager.update();
	}

	@Test
	public void runSanitizer() throws IOException {
		sanitizer.sanitize();
	}

}
