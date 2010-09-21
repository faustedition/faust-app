package de.faustedition;

import java.util.logging.Level;
import java.util.logging.Logger;

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
        final long startTime = System.currentTimeMillis();
        try {
            final ArchiveManager archiveManager = injector.getInstance(ArchiveManager.class);
            final TranscriptManager transcriptManager = injector.getInstance(TranscriptManager.class);
            final DocumentManager documentManager = injector.getInstance(DocumentManager.class);
            final XMLStorage xml = injector.getInstance(XMLStorage.class);

            logger.info("Importing archives");
            archiveManager.synchronize();

            logger.info("Importing sample transcriptions");
            for (FaustURI transcript : xml.iterate(new FaustURI(FaustAuthority.XML, "/transcript/gsa/390883"))) {
                transcriptManager.add(transcript);
            }

            logger.info("Importing documents");
            for (FaustURI documentDescriptor : xml.iterate(DocumentManager.DOCUMENT_BASE_URI)) {
                logger.info("Importing document " + documentDescriptor);
                documentManager.add(documentDescriptor);
            }
            
            logger.info(String.format("Import finished in %.3f seconds", (System.currentTimeMillis() - startTime) / 1000.0f));
            System.exit(0);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while importing data", e);
            System.exit(1);
        }
    }
}
