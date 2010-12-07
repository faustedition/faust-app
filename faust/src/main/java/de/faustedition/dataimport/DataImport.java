package de.faustedition.dataimport;

import java.util.SortedSet;
import java.util.TreeSet;
import java.util.logging.Logger;

import com.google.common.base.Joiner;
import com.google.inject.Inject;

import de.faustedition.FaustURI;
import de.faustedition.Runtime;
import de.faustedition.document.ArchiveManager;
import de.faustedition.document.MaterialUnitManager;
import de.faustedition.genesis.GeneticRelationManager;
import de.faustedition.text.TextManager;
import de.faustedition.transcript.TranscriptManager;

public class DataImport extends Runtime implements Runnable {

	private final ArchiveManager archiveManager;
	private final TranscriptManager transcriptManager;
	private final MaterialUnitManager documentManager;
	private final TextManager textManager;
	private final Logger logger;
	private final GeneticRelationManager geneticRelationManager;

	@Inject
	public DataImport(ArchiveManager archiveManager, TranscriptManager transcriptManager, MaterialUnitManager documentManager,
			TextManager textManager, GeneticRelationManager geneticRelationManager, Logger logger) {
		this.archiveManager = archiveManager;
		this.transcriptManager = transcriptManager;
		this.documentManager = documentManager;
		this.textManager = textManager;
		this.geneticRelationManager = geneticRelationManager;
		this.logger = logger;
	}

	public static void main(String[] args) throws Exception {
		try {
			main(DataImport.class, args);
		} finally {
			System.exit(0);
		}
	}

	@Override
	public void run() {
		logger.info("Importing all data into graph");
		final SortedSet<FaustURI> failed = new TreeSet<FaustURI>();
		final long startTime = System.currentTimeMillis();

		archiveManager.feedGraph();
		failed.addAll(transcriptManager.feedGraph());
		failed.addAll(documentManager.feedGraph());
		failed.addAll(textManager.feedGraph());
		geneticRelationManager.feedGraph();

		logger.info(String.format("Import finished in %.3f seconds", (System.currentTimeMillis() - startTime) / 1000.0f));
		if (!failed.isEmpty()) {
			logger.severe("Failed imports:\n" + Joiner.on("\n").join(failed));
		}
	}
}
