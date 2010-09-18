package de.faustedition.db;

import org.junit.Test;

import com.google.inject.Guice;

import de.faustedition.ConfigurationModule;
import de.faustedition.DataAccessModule;
import de.faustedition.FaustURI;
import de.faustedition.document.DocumentManager;
import de.faustedition.xml.XMLStorage;

public class XMLStorageTest {

    @Test
    public void listStorageContents() {
        XMLStorage storage = Guice.createInjector(new ConfigurationModule(), new DataAccessModule()).getInstance(XMLStorage.class);
        for (FaustURI xml : storage.iterate(DocumentManager.DOCUMENT_BASE_URI)) {
            System.out.println(xml);
        }
    }
}
