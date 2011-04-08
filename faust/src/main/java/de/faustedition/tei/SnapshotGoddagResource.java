package de.faustedition.tei;

import java.util.logging.Level;
import java.util.logging.Logger;

import org.goddag4j.MultiRootedTree;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Transaction;

import com.google.inject.Inject;

import de.faustedition.transcript.Transcript;
import de.faustedition.transcript.TranscriptManager;

public class SnapshotGoddagResource extends GoddagResource {

	private final TranscriptManager transcriptManager;
	private final Logger logger;
	
	@Inject
	public SnapshotGoddagResource(GraphDatabaseService db, TranscriptManager transcriptManager, Logger logger) {
		super(db);
		this.transcriptManager = transcriptManager;
		this.logger = logger;		
	}

	@Override
	public MultiRootedTree trees() {
		try {
			if (transcriptType != null) {
				for (Transcript transcript : transcriptManager.parse(source)) {
					if (transcript.getType() == transcriptType) {
						transcript.postprocess();
						return transcript.getTrees();
					}
				}
				
			}
		} catch (Exception e) {
			logger.log(Level.SEVERE, "Error while parsing snapshot of transcript " + source, e);
		}
		
		return super.trees();
	}
	
	@Override
	protected void inTransaction(InTransactionCallback callback) throws Exception {
		final Transaction tx = db.beginTx();
		try {
			callback.inTransaction();
			tx.failure();
		} finally {
			tx.finish();
		}
	}
}
