package de.faustedition.bibliography;

import org.junit.Test;

import com.hp.hpl.jena.rdf.model.Model;
import com.hp.hpl.jena.rdf.model.ModelFactory;

public class ZoteroExportTest {

    @Test
    public void readRdfModel() throws Exception {
        final Model bibliography = ModelFactory.createDefaultModel();
        bibliography.read(getClass().getResource("zotero-export.rdf").toString());
        bibliography.write(System.out);
    }
}
