package de.faustedition.xml;

import static de.faustedition.xml.NodeListIterable.singleResult;
import static de.faustedition.xml.XmlDocument.xpath;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.net.URI;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Element;

import de.faustedition.AbstractContextTest;

public class XmlDatabaseConnectionTest extends AbstractContextTest {
	private Logger logger = LoggerFactory.getLogger(XmlDatabaseConnectionTest.class);

	@Autowired
	private XmlDbManager manager;

	@Test
	public void retrieveResources() {
		assertNotNull(singleResult(xpath("//f:resource[starts-with(text(), 'Witness/')]"), manager.resources(), Element.class));
	}

	@Test
	public void readContents() {
		if (logger.isTraceEnabled()) {
			readCollection(URI.create(""));
		}
		assertTrue("XML database contains resources", !manager.contentsOf(URI.create("Witness/")).isEmpty());
	}

	private void readCollection(URI uri) {
		for (URI content : manager.contentsOf(uri)) {
			logger.trace(content.toString());
			if (XmlDbManager.isCollectionURI(content)) {
				readCollection(content);
			}
		}
	}
}
