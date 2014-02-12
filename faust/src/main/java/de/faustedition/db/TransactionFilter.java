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

import static de.faustedition.db.ChainedTransactionManager.TX_ATTRIBUTE;

import java.util.Map;

import org.restlet.Context;
import org.restlet.Request;
import org.restlet.Response;
import org.restlet.Restlet;
import org.restlet.routing.Filter;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.DefaultTransactionDefinition;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TransactionFilter extends Filter {
	private final PlatformTransactionManager transactionManager;

	public TransactionFilter(Context context, Restlet next, PlatformTransactionManager transactionManager) {
		super(context, next);
		this.transactionManager = transactionManager;
	}

	@Override
	protected int beforeHandle(Request request, Response response) {
		final Map<String, Object> responseAttributes = response.getAttributes();
		if (!responseAttributes.containsKey(TX_ATTRIBUTE)) {
			responseAttributes.put(TX_ATTRIBUTE, transactionManager.getTransaction(new DefaultTransactionDefinition()));
		}
		return super.beforeHandle(request, response);
	}

	@Override
	protected void afterHandle(Request request, Response response) {
		TransactionStatus tx = (TransactionStatus) response.getAttributes().remove(TX_ATTRIBUTE);
		if (tx != null) {
			if (response.getStatus().isError()) {
				tx.setRollbackOnly();
			}
			transactionManager.commit(tx);
		}
		super.afterHandle(request, response);
	}
}
