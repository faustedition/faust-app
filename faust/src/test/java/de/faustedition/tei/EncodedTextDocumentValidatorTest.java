package de.faustedition.tei;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;
import de.faustedition.Log;
import de.faustedition.xml.XmlUtil;

public class EncodedTextDocumentValidatorTest extends AbstractContextTest {

	@Autowired
	private EncodedTextDocumentBuilder builder;

	@Autowired
	private EncodedTextDocumentValidator validator;

	@Test
	public void validateTemplate() throws Exception {
		EncodedTextDocument document = builder.create();
		if (Log.LOGGER.isTraceEnabled()) {
			XmlUtil.serialize(document.getDom(), System.out);
		}

		List<String> errors = validator.validate(document);
		Assert.assertTrue("Template document validates", errors.isEmpty());
		if (errors.size() > 0 && Log.LOGGER.isTraceEnabled()) {
			for (String error : errors) {
				Log.LOGGER.trace(error);
			}
		}
	}

	@Test
	public void validateSample() throws Exception {
		EncodedTextDocument document = new EncodedTextDocument(XmlUtil.parse(getClass().getResourceAsStream("faust-tei-sample.xml")));
		List<String> errors = validator.validate(document);
		if (errors.size() > 0 && Log.LOGGER.isTraceEnabled()) {
			for (String error : errors) {
				Log.LOGGER.trace(error);
			}
		}
		Assert.assertTrue("Test document validates", errors.isEmpty());
	}
}
