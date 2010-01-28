package de.faustedition.model.tei;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.model.AbstractModelContextTest;
import de.faustedition.util.XMLUtil;

public class EncodedTextDocumentManagerTest extends AbstractModelContextTest {

	@Autowired
	private EncodedTextDocumentManager documentManager;

	@Test
	public void enhanceNewDocument() {
		EncodedTextDocument d = documentManager.process(EncodedTextDocument.create());
		XMLUtil.serialize(d.getDom(), System.out, true);
	}
}
