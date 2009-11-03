package de.faustedition.util;

import java.io.StringWriter;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import junit.framework.Assert;

import org.apache.commons.lang.StringUtils;
import org.junit.Test;

public class SaxonXSLTSetupTest
{
	@Test
	public void testSaxonTransformer() throws TransformerException
	{
		Transformer transformer = XMLUtil.saxonTransformerFactory().newTransformer();
		StringWriter transformationResult = new StringWriter();
		transformer.transform(new DOMSource(XMLUtil.parse("<?xml version=\"1.0\"?><root/>".getBytes())), new StreamResult(transformationResult));
		Assert.assertTrue(StringUtils.isNotBlank(transformationResult.toString()));
	}
}
