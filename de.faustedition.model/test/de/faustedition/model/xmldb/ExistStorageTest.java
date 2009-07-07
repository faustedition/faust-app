package de.faustedition.model.xmldb;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
import org.w3c.dom.Document;

import de.faustedition.model.xmldb.ExistQueryParameters;
import de.faustedition.model.xmldb.ExistDatabase;
import de.faustedition.util.XMLUtil;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/module-context.xml" })
public class ExistStorageTest {

	@Autowired
	private ExistDatabase database;
	
	@Test
	public void rootQuery() throws Exception {
		ExistQueryParameters parameters = ExistQueryParameters.createDefault();
		parameters.setIndent(true);
		
		Document testDocument = null;
		for (int i = 0; i < 10; i++) {
			testDocument = database.get(Collection.ROOT.getPath(), parameters);
		}
		XMLUtil.serialize(testDocument, System.out);
	}
}
