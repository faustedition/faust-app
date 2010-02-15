package de.swkk.metadata;

import java.net.URI;

import org.apache.commons.io.FilenameUtils;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.faustedition.xml.XmlDbManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/de/faustedition/tei/context.xml", "/de/faustedition/xml/context.xml", "context.xml" })
public class PurgeLegacyMetadata {

	@Autowired
	private XmlDbManager xmlDbManager;

	@Test
	public void purgeLegacyFiles() {
		for (URI resource : xmlDbManager.resourceUris()) {
			if ("metadata.xml".equals(FilenameUtils.getName(resource.getPath()))) {
				xmlDbManager.delete(resource);
			}
		}
	}
}
