package de.faustedition.model;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import de.faustedition.model.metadata.ManuscriptIdentifier;
import de.faustedition.model.service.TEIDocumentManager;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations = { "/META-INF/spring/module-context.xml" })
public class TEiDocumentTest {
	@Autowired
	private TEIDocumentManager documentManager;

	@Test
	public void documentCreationAndSerialization() throws Exception {
		documentManager.serialize(documentManager.createDocument(), System.out);
	}
	
	@Test
	public void titleAddition() throws Exception {
		TEIDocument teiDocument = documentManager.createDocument();
		documentManager.setTitle(teiDocument, "Hello World");
		documentManager.serialize(teiDocument, System.out);
	}
	
	@Test
	public void manuscriptIdentifierSetting() throws Exception {
		TEIDocument teiDocument = documentManager.createDocument();
		ManuscriptIdentifier msIdentifier = new ManuscriptIdentifier();
		msIdentifier.setInstitution("Klassik-Stiftung Weimar");
		msIdentifier.setRepository("Goethe- und Schiller-Archiv");
		msIdentifier.setCollection("Werke");
		msIdentifier.getIdentifiers().put("archive-db", "123456");
		documentManager.setManuscriptIdentifier(teiDocument, msIdentifier);
		documentManager.serialize(teiDocument, System.out);
	}
}
