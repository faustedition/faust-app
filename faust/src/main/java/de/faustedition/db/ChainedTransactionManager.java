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

package de.faustedition.db;

import static java.util.Arrays.asList;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.transaction.HeuristicCompletionException;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionException;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.UnexpectedRollbackException;

/**
 * @author mh
 * @since 14.02.11
 */
public class ChainedTransactionManager implements PlatformTransactionManager {

	private final static Log logger = LogFactory.getLog(ChainedTransactionManager.class);
	static final String TX_ATTRIBUTE = "tx";

	private final List<PlatformTransactionManager> transactionManagers;
	private final SynchronizationManager synchronizationManager;

	public ChainedTransactionManager(PlatformTransactionManager... transactionManagers) {
		this(new DefaultSynchronizationManager(),transactionManagers);
	}

	public ChainedTransactionManager(SynchronizationManager synchronizationManager, PlatformTransactionManager... transactionManagers) {
		this.synchronizationManager = synchronizationManager;
		this.transactionManagers=asList(transactionManagers);
	}

	@Override
	public MultiTransactionStatus getTransaction(TransactionDefinition definition) throws TransactionException {

		MultiTransactionStatus mts = new MultiTransactionStatus(transactionManagers.get(0)/*First TM is main TM*/);

		if (!synchronizationManager.isSynchronizationActive()) {
			synchronizationManager.initSynchronization();
			mts.setNewSynchonization();
		}

		for (PlatformTransactionManager transactionManager : transactionManagers) {
			mts.registerTransactionManager(definition, transactionManager);
		}

		return mts;
	}

	@Override
	public void commit(TransactionStatus status) throws TransactionException {

		MultiTransactionStatus multiTransactionStatus = (MultiTransactionStatus) status;

		boolean commit = true;
		Exception commitException = null;
		PlatformTransactionManager commitExceptionTransactionManager = null;

		for (PlatformTransactionManager transactionManager : reverse(transactionManagers)) {
			if (commit) {
				try {
					multiTransactionStatus.commit(transactionManager);
				} catch (Exception ex) {
					commit = false;
					commitException = ex;
					commitExceptionTransactionManager = transactionManager;
				}
			} else {
				//after unsucessfull commit we must try to rollback remaining transaction managers
				try {
					multiTransactionStatus.rollback(transactionManager);
				} catch (Exception ex) {
					logger.warn("Rollback exception (after commit) (" + transactionManager + ") " + ex.getMessage(), ex);
				}
			}
		}

		if (multiTransactionStatus.isNewSynchonization()){
			synchronizationManager.clearSynchronization();
		}

		if (commitException != null) {
			boolean firstTransactionManagerFailed = commitExceptionTransactionManager == getLastTransactionManager();
			int transactionState = firstTransactionManagerFailed ? HeuristicCompletionException.STATE_ROLLED_BACK : HeuristicCompletionException.STATE_MIXED;
			throw new HeuristicCompletionException(transactionState, commitException);
		}

	}

	@Override
	public void rollback(TransactionStatus status) throws TransactionException {

		Exception rollbackException = null;
		PlatformTransactionManager rollbackExceptionTransactionManager = null;


		MultiTransactionStatus multiTransactionStatus = (MultiTransactionStatus) status;

		for (PlatformTransactionManager transactionManager : reverse(transactionManagers)) {
			try {
				multiTransactionStatus.rollback(transactionManager);
			} catch (Exception ex) {
				if (rollbackException == null) {
					rollbackException = ex;
					rollbackExceptionTransactionManager = transactionManager;
				} else {
					logger.warn("Rollback exception (" + transactionManager + ") " + ex.getMessage(), ex);
				}
			}
		}

		if (multiTransactionStatus.isNewSynchonization()){
			synchronizationManager.clearSynchronization();
		}

		if (rollbackException != null) {
			throw new UnexpectedRollbackException("Rollback exception, originated at ("+rollbackExceptionTransactionManager+") "+
				rollbackException.getMessage(), rollbackException);
		}
	}

	private <T> Iterable<T> reverse(Collection<T> collection) {
		List<T> list = new ArrayList<T>(collection);
		Collections.reverse(list);
		return list;
	}


	private PlatformTransactionManager getLastTransactionManager() {
		return transactionManagers.get(lastTransactionManagerIndex());
	}

	private int lastTransactionManagerIndex() {
		return transactionManagers.size() - 1;
	}

}