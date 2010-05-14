package de.faustedition.xml;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;

public class XmlStoreTest extends AbstractContextTest {
	private Logger logger = LoggerFactory.getLogger(XmlStoreTest.class);

	@Autowired
	private XmlStore store;

	@Test
	public void readContents() throws IOException {
		assertTrue("XML database contains resources", !store.list(URI.create("Witness/")).isEmpty());
		if (logger.isTraceEnabled()) {
			logContents(URI.create(""));
		}
	}

	private void logContents(URI uri) throws IOException {
		for (URI content : store.list(uri)) {
			logger.trace(content.toString());
			logContents(content);
		}
	}
}
