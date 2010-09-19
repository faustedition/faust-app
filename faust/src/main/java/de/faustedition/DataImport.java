package de.faustedition;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;

import com.google.inject.Module;

import de.faustedition.document.ArchiveManager;
import de.faustedition.document.DocumentManager;
import de.faustedition.inject.ConfigurationModule;
import de.faustedition.inject.DataAccessModule;
import de.faustedition.transcript.TranscriptManager;
import de.faustedition.xml.XMLStorage;

public class DataImport extends MainBase implements Runnable {

    public static void main(String[] args) {
        DataImport dataImport = new DataImport();
        dataImport.init(args);
        dataImport.run();
    }

    @Override
    protected Module[] createModules() {
        return new Module[] { new ConfigurationModule(), new DataAccessModule() };
    }

    @Override
    public void run() {
        final Logger logger = Logger.getLogger(getClass().getName());        

        try {
            final ArchiveManager archiveManager = injector.getInstance(ArchiveManager.class);
            final TranscriptManager transcriptManager = injector.getInstance(TranscriptManager.class);
            final DocumentManager documentManager = injector.getInstance(DocumentManager.class);
            final XMLStorage xml = injector.getInstance(XMLStorage.class);
            
            logger.info("Importing archives");
            archiveManager.synchronize();
            
            logger.info("Importing sample transcriptions");
            for (FaustURI transcript : xml.iterate(new FaustURI(FaustURI.Authority.XML, "/transcript/gsa/390883"))) {
                transcriptManager.add(transcript);
            }
            
            logger.info("Importing documents");
            for (FaustURI documentDescriptor : xml.iterate(DocumentManager.DOCUMENT_BASE_URI)) {
                final InputSource ddSource = xml.getInputSource(documentDescriptor);
                try {
                    documentManager.add(ddSource);
                } finally {
                    IOUtils.closeQuietly(ddSource.getByteStream());
                }
            }
            System.exit(0);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while importing data", e);
            System.exit(1);
        }
    }
}
