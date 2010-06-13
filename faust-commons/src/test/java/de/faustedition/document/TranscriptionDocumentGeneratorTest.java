package de.faustedition.document;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;

public class TranscriptionDocumentGeneratorTest extends AbstractContextTest {

	@Autowired
	private TranscriptionDocumentGenerator generator;

	@Test
	public void generate() {
		generator.run();
	}
}
