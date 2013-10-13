package de.faustedition;

import org.junit.Assert;

import org.junit.Test;

public class URITest {

	@Test
	public void encodingClassification() {
		final FaustURI textEncodingDoc = xmlUri("/transcript/gsa/391098/391098.xml");
		Assert.assertTrue(textEncodingDoc.isWitnessEncodingDocument());
		Assert.assertTrue(textEncodingDoc.isTextEncodingDocument());
		Assert.assertTrue(!textEncodingDoc.isDocumentEncodingDocument());

		final FaustURI docEncodingDoc = xmlUri("/transcript/gsa/391098/0001.xml");
		Assert.assertTrue(docEncodingDoc.isWitnessEncodingDocument());
		Assert.assertTrue(!docEncodingDoc.isTextEncodingDocument());
		Assert.assertTrue(docEncodingDoc.isDocumentEncodingDocument());
}
	
	private FaustURI xmlUri(String path) {
		return new FaustURI(FaustAuthority.XML, path);
	}
}
