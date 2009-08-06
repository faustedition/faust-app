package de.faustedition.model;

import org.junit.Test;

public class TEiDocumentTest {

	@Test
	public void documentCreation() throws Exception {
		TEIDocument.createInstance().serialize(System.out);
	}
}
