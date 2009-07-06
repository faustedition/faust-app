package de.faustedition.util;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;

public class XMLUtilTest {
	@Test
	public void addProcessingInstruction() throws Exception {
		Document document = XMLUtil.createDocumentBuilder().newDocument();
		XMLUtil.addProcessingInstruction(document, "pi", "key\"value\"");
		Assert.assertEquals(1, XMLUtil.processingInstructions(document, "pi").size());
	}

	@Test
	public void removeProcessingInstruction() throws Exception {
		Document document = XMLUtil.createDocumentBuilder().newDocument();
		XMLUtil.addProcessingInstruction(document, "pi", "key\"value\"");
		XMLUtil.removeProcessingInstruction(document, "pi");
		Assert.assertEquals(0, XMLUtil.processingInstructions(document, "pi").size());
	}
}
