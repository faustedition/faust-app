package de.faustedition;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.inject.Module;

import de.faustedition.document.ArchiveManager;
import de.faustedition.document.MaterialUnitManager;
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
        final SortedSet<FaustURI> failed = new TreeSet<FaustURI>();
        final long startTime = System.currentTimeMillis();
        try {
            final ArchiveManager archiveManager = injector.getInstance(ArchiveManager.class);
            final TranscriptManager transcriptManager = injector.getInstance(TranscriptManager.class);
            final MaterialUnitManager documentManager = injector.getInstance(MaterialUnitManager.class);
            final XMLStorage xml = injector.getInstance(XMLStorage.class);

            logger.info("Importing archives");
            archiveManager.synchronize();

            logger.info("Importing sample transcriptions");
            // "/transcript/gsa/390883"
            for (FaustURI transcript : xml.iterate(new FaustURI(FaustAuthority.XML, "/transcript"))) {
                try {
                    logger.info("Importing transcript " + transcript);
                    transcriptManager.add(transcript);
                } catch (SAXException e) {
                    logger.log(Level.SEVERE, "XML error while adding transcript " + transcript, e);
                    failed.add(transcript);
                }
            }

            logger.info("Importing documents");
            for (FaustURI documentDescriptor : xml.iterate(MaterialUnitManager.DOCUMENT_BASE_URI)) {
                try {
                    logger.info("Importing document " + documentDescriptor);
                    documentManager.add(documentDescriptor);
                } catch (SAXException e) {
                    logger.log(Level.SEVERE, "XML error while adding document " + documentDescriptor, e);
                    failed.add(documentDescriptor);
                }
            }

            logger.info(String.format("Import finished in %.3f seconds", (System.currentTimeMillis() - startTime) / 1000.0f));
            if (!failed.isEmpty()) {
                logger.info("\nFailed imports:\n" + Joiner.on("\n").join(failed));
            }
            System.exit(0);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while importing data", e);
            System.exit(1);
        }
    }
}
