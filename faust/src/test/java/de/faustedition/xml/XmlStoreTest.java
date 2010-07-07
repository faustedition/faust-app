package de.faustedition.xml;

import static org.junit.Assert.assertTrue;

import java.io.IOException;
import java.net.URI;

import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;

import de.faustedition.AbstractContextTest;
import de.faustedition.Log;

public class XmlStoreTest extends AbstractContextTest {
	@Autowired
	private XmlStore store;

	@Test
	public void readContents() throws IOException {
		assertTrue("XML database contains resources", !store.list(URI.create("Witness/")).isEmpty());
		if (Log.LOGGER.isTraceEnabled()) {
			for (URI uri : store) {
				Log.LOGGER.trace(uri.toString());
			}
		}
	}
}
