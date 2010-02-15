package de.faustedition.xml;

import static de.faustedition.xml.NodeListIterable.singleResult;
import static de.faustedition.xml.XmlDocument.xpath;
import static org.junit.Assert.assertNotNull;

import java.net.URI;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import de.faustedition.AbstractTest;

public class XmlDatabaseConnection extends AbstractTest {

	@Autowired
	private XmlDbManager manager;

	@Test
	public void retrieveResources() {
		Document resources = manager.resources();
		assertNotNull(singleResult(xpath("//f:resource[starts-with(text(), 'Witness/')]"), resources, Element.class));
	}
	
	@Test
	public void readContents() {
		readCollection(URI.create(""));
		for (URI content : manager.contentsOf(URI.create("Witness/"))) {
			System.out.println(content);
		}
	}

	private void readCollection(URI uri) {
		for (URI content : manager.contentsOf(uri)) {
			System.out.println(content);
			if (XmlDbManager.isCollectionURI(content)) {
				readCollection(content);
			}
		}		
	}
}
