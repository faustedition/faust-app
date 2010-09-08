package de.faustedition.metadata;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/de/faustedition/tei/context.xml", "/de/faustedition/xml/context.xml", "context.xml" })
public class MetadataImportRun {

	@Autowired
	private MetadataImportTask importTask;
	
	@Test
	public void runImport() {
		importTask.run();
	}
}
