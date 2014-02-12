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

package de.faustedition;

import com.google.common.base.Throwables;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.map.ObjectMapper;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.WriterRepresentation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.TransactionStatus;
import org.springframework.transaction.support.TransactionCallbackWithoutResult;
import org.springframework.transaction.support.TransactionTemplate;

import java.io.IOException;
import java.io.Writer;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Service
public class JsonRepresentationFactory {

	@Autowired
	private TransactionTemplate transactionTemplate;

  @Autowired
	private ObjectMapper objectMapper;

	public Representation map(Object object) {
		return map(object, true);
	}

	public Representation map(final Object object, final boolean transactional) {
		return new WriterRepresentation(MediaType.APPLICATION_JSON) {
			@Override
			public void write(final Writer writer) throws IOException {
				if (transactional) {
					try {
						transactionTemplate.execute(new TransactionCallbackWithoutResult() {
							@Override
							protected void doInTransactionWithoutResult(TransactionStatus status) {
								try {
									writeJson(writer);
								} catch (IOException e) {
									throw Throwables.propagate(e);
								}
							}
						});
					} catch (Throwable t) {
						Throwables.propagateIfInstanceOf(Throwables.getRootCause(t), IOException.class);
						throw Throwables.propagate(t);
					}
				} else {
					writeJson(writer);
				}

			}

			protected void writeJson(Writer writer) throws IOException {
				final JsonGenerator jg = objectMapper.getJsonFactory().createJsonGenerator(writer);
				jg.writeObject(object);
				jg.flush();
			}
		};
	}
}
