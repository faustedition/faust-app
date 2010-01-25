package de.faustedition.model;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

import de.faustedition.model.tei.EncodedTextDocument;
import de.faustedition.util.XMLUtil;

public class TEIDocumentTest {

	private EncodedTextDocument teiDocument = EncodedTextDocument.create();

	@Test
	public void parse() throws Exception {
		Assert.assertNotNull(XMLUtil.parse(new String("<TEI/>").getBytes("UTF-8")));
	}

	@Test
	public void serialize() {
		XMLUtil.serialize(teiDocument.getDocument(), System.out, true);
	}

	@Test
	public void xpath() {
		Assert.assertTrue(teiDocument.xpath("/:TEI").iterator().hasNext());
	}

	@Test
	public void findNode() {
		Element tei = teiDocument.findNode("/:TEI", Element.class);
		Assert.assertNotNull(tei);
		Assert.assertEquals("TEI", tei.getLocalName());
	}
}
