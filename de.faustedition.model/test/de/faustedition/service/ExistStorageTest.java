package de.faustedition.service;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

import de.faustedition.model.xmldb.ExistQueryParameters;
import de.faustedition.model.xmldb.ExistXmlStorage;
import de.faustedition.util.XMLUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/module-context.xml" })
public class ExistStorageTest {

	@Autowired
	private ExistXmlStorage storage;
	
	@Test
	public void rootQuery() throws Exception {
		ExistQueryParameters parameters = ExistQueryParameters.createDefault();
		parameters.setIndent(true);
		
		Document testDocument = null;
		for (int i = 0; i < 10; i++) {
			testDocument = storage.get("db/twitter/existdb_user.xml", parameters);
		}
		XMLUtil.serialize(testDocument, System.out);
	}
}
