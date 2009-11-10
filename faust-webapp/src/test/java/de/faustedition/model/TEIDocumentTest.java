package de.faustedition.model;

import org.junit.Assert;
import org.junit.Test;
import org.w3c.dom.Document;
import org.xml.sax.SAXParseException;

import de.faustedition.model.tei.TEIDocument;
import de.faustedition.util.XMLUtil;

public class TEIDocumentTest
{

	@Test
	public void parse() throws Exception
	{
		Assert.assertNotNull(XMLUtil.parse(new String("<TEI/>").getBytes("UTF-8")));
	}

	@Test
	public void validate() throws Exception
	{
		Document testDocument = TEIDocument.buildTemplate("Hello World").getDocument();
		XMLUtil.serialize(testDocument, System.out, true);
		for (SAXParseException error : TEIDocument.validate(testDocument))
		{
			System.out.printf("\n[%d:%d] %s", error.getLineNumber(), error.getColumnNumber(), error.getLocalizedMessage());
		}
	}
}
