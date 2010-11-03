package de.faustedition;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.xml.sax.SAXException;

import com.google.common.base.Joiner;
import com.google.inject.Inject;

import de.faustedition.document.ArchiveManager;
import de.faustedition.document.MaterialUnitManager;
import de.faustedition.text.TextManager;
import de.faustedition.transcript.TranscriptManager;
import de.faustedition.xml.XMLStorage;

public class DataImport extends Runtime implements Runnable {

    private final ArchiveManager archiveManager;
    private final TranscriptManager transcriptManager;
    private final MaterialUnitManager documentManager;
    private final TextManager textManager;
    private final XMLStorage xml;
    private final Logger logger;

    @Inject
    public DataImport(ArchiveManager archiveManager, TranscriptManager transcriptManager, MaterialUnitManager documentManager,
            TextManager textManager, XMLStorage xml, Logger logger) {
        this.archiveManager = archiveManager;
        this.transcriptManager = transcriptManager;
        this.documentManager = documentManager;
        this.textManager = textManager;
        this.xml = xml;
        this.logger = logger;
    }

    public static void main(String[] args) throws Exception {
        main(DataImport.class, args);
    }

    @Override
    public void run() {
        final SortedSet<FaustURI> failed = new TreeSet<FaustURI>();
        final long startTime = System.currentTimeMillis();
        try {
            logger.info("Importing archives");
            archiveManager.synchronize();

            logger.info("Importing sample transcriptions");
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

            logger.info("Importing texts");
            for (FaustURI textSource : xml.iterate(new FaustURI(FaustAuthority.XML, "/text"))) {
                try {
                    logger.info("Importing text " + textSource);
                    textManager.add(textSource);
                } catch (SAXException e) {
                    logger.log(Level.SEVERE, "XML error while adding text " + textSource, e);
                    failed.add(textSource);
                }
            }

            logger.info(String.format("Import finished in %.3f seconds", (System.currentTimeMillis() - startTime) / 1000.0f));
            if (!failed.isEmpty()) {
                logger.severe("\nFailed imports:\n" + Joiner.on("\n").join(failed));
            }
            System.exit(0);
        } catch (Exception e) {
            logger.log(Level.SEVERE, "Error while importing data", e);
            System.exit(1);
        }
    }
}
