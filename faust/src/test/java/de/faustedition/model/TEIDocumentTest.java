package de.faustedition.model;

import static de.faustedition.model.tei.EncodedTextDocument.xpath;
import static de.faustedition.model.xml.NodeListIterable.singleResult;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.w3c.dom.Element;

import de.faustedition.model.tei.EncodedTextDocument;
import de.faustedition.model.xml.XmlUtil;

public class TEIDocumentTest {

	private EncodedTextDocument teiDocument;

	@Before
	public void createDocument() {
		teiDocument = EncodedTextDocument.create();
	}

	@Test
	public void parse() throws Exception {
		Assert.assertNotNull(XmlUtil.parse(new String("<TEI/>").getBytes("UTF-8")));
	}

	@Test
	public void serialize() {
		XmlUtil.serialize(teiDocument.getDom(), System.out, true);
	}

	@Test
	public void xpathEvaluation() {
		Assert.assertNotNull(singleResult(xpath("/tei:TEI"), teiDocument.getDom(), Element.class));
	}
}
