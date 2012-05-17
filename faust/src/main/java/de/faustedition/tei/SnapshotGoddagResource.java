package de.faustedition.tei;

import de.faustedition.transcript.GoddagTranscript;
import de.faustedition.transcript.GoddagTranscriptManager;
import org.goddag4j.MultiRootedTree;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class SnapshotGoddagResource extends GoddagResource {

	@Autowired
	private GoddagTranscriptManager transcriptManager;

	@Autowired
	private Logger logger;
	
	@Override
	public MultiRootedTree trees() {
		try {
			if (transcriptType != null) {
				for (GoddagTranscript transcript : transcriptManager.parse(source)) {
					if (transcript.getType() == transcriptType) {
						transcript.postprocess();
						return transcript.getTrees();
					}
				}
				
			}
		} catch (Exception e) {
			logger.error("Error while parsing snapshot of transcript " + source, e);
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
