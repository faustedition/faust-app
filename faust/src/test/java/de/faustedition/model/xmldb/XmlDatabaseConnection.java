package de.faustedition.model.xmldb;

import static de.faustedition.model.XmlDocument.xpath;
import static de.faustedition.model.xmldb.NodeListIterable.singleResult;
import static org.junit.Assert.assertNotNull;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.model.AbstractModelContextTest;

public class XmlDatabaseConnection extends AbstractModelContextTest {

	@Autowired
	private XmlDbManager manager;

	@Test
	public void retrieveResources() {
		Document resources = manager.resources();
		assertNotNull(singleResult(xpath("//f:resource[starts-with(text(), 'Witness/')]"), resources, Element.class));
	}
}
