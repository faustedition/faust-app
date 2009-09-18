package de.faustedition.model;

import javax.xml.XMLConstants;
import javax.xml.validation.SchemaFactory;

import org.junit.Assert;
import org.junit.Test;

public class RelaxNGValidatorAvailability {

	@Test
	public void retrieveRelaxNGValidatorFactory() throws Exception {
		Assert.assertNotNull(SchemaFactory.newInstance(XMLConstants.RELAXNG_NS_URI));
	}
}
