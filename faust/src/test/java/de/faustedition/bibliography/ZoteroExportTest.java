package de.faustedition.bibliography;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

import de.faustedition.AbstractContextTest;

public class ZoteroExportTest extends AbstractContextTest {

	@Test
	public void readRdfModel() throws Exception {
		Model bibliography = ModelFactory.createDefaultModel();
		bibliography.read(new ClassPathResource("zotero-export.rdf", getClass()).getInputStream(), null);
		bibliography.write(System.out);
	}
}
