package de.faustedition.model;

import static de.faustedition.tei.EncodedTextDocument.xpath;
import static de.faustedition.xml.NodeListIterable.singleResult;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Element;

import de.faustedition.tei.EncodedTextDocument;
import de.faustedition.xml.XmlUtil;

public class TEIDocumentTest {

	private EncodedTextDocument teiDocument = EncodedTextDocument.create("TEI");

	@Test
	public void parse() throws Exception {
		Assert.assertNotNull(XmlUtil.parse(new String("<TEI/>").getBytes("UTF-8")));
	}

	@Test
	public void serialize() {
		XmlUtil.serialize(teiDocument.getDom(), System.out);
	}

	@Test
	public void xpathEvaluation() {
		Assert.assertNotNull(singleResult(xpath("/tei:TEI"), teiDocument.getDom(), Element.class));
	}
}
