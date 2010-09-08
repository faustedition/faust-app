package de.faustedition.db;

import java.io.File;

import org.junit.Test;

import com.google.inject.Guice;

import de.faustedition.ServerModule;

public class XmlStorageTest {

    @Test
    public void listStorageContents() {
        XmlStorage storage = Guice.createInjector(new ServerModule()).getInstance(XmlStorage.class);
        for (File xml : storage) {
            System.out.println(xml.getAbsolutePath());
        }
    }
}
