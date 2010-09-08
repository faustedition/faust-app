package de.faustedition.metadata;

import java.io.IOException;
import java.net.URI;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.faustedition.xml.XmlStore;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/de/faustedition/tei/context.xml", "/de/faustedition/xml/context.xml", "context.xml" })
public class PurgeLegacyMetadata {

	@Autowired
	private XmlStore xmlStore;

	@Test
	public void purgeLegacyFiles() throws IOException {
		for (URI resource : xmlStore) {
			if ("metadata.xml".equals(FilenameUtils.getName(resource.getPath()))) {
				xmlStore.delete(resource);
			}
		}
	}
}
