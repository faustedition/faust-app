/*
 * Copyright (c) 2014 Faust Edition development team.
 *
 * This file is part of the Faust Edition.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Affero General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Affero General Public License for more details.
 *
 * You should have received a copy of the GNU Affero General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package de.faustedition.tei;

import org.goddag4j.MultiRootedTree;
import org.neo4j.graphdb.Transaction;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.faustedition.transcript.GoddagTranscript;
import de.faustedition.transcript.GoddagTranscriptManager;

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
