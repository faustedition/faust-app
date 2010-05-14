package de.faustedition.tei;

import java.util.List;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;
import de.faustedition.xml.XmlUtil;

public class EncodedTextDocumentValidatorTest extends AbstractContextTest {

	@Autowired
	private EncodedTextDocumentManager documentManager;

	@Autowired
	private EncodedTextDocumentValidator validator;

	@Test
	public void checkForInvalidity() throws Exception {
		EncodedTextDocument document = documentManager.create();
		if (LOG.isTraceEnabled()) {
			XmlUtil.serialize(document.getDom(), System.out);
		}

		List<String> errors = validator.validate(document);
		Assert.assertTrue("Validation errors exist", errors.size() > 0);
		if (LOG.isTraceEnabled()) {
			for (String error : errors) {
				LOG.trace(error);
			}
		}
	}

	@Test
	public void checkForValidity() throws Exception {
		EncodedTextDocument document = new EncodedTextDocument(XmlUtil.parse(getClass().getResourceAsStream("faust-tei-sample.xml")));
		List<String> errors = validator.validate(document);
		Assert.assertTrue("Test document validates", errors.isEmpty());
		if (errors.size() > 0 && LOG.isTraceEnabled()) {
			for (String error : errors) {
				LOG.trace(error);
			}
		}
	}
}
