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

package de.faustedition.transcript;

import com.google.common.base.Objects;
import de.faustedition.document.MaterialUnit;
import de.faustedition.template.TemplateRepresentationFactory;
import de.faustedition.xml.XMLStorage;
import eu.interedition.text.Layer;
import org.codehaus.jackson.JsonNode;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.NotFoundException;
import org.restlet.data.Status;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public abstract class TranscriptResource extends ServerResource {
	@Autowired
	protected TemplateRepresentationFactory templateFactory;

	@Autowired
	protected GraphDatabaseService db;

	@Autowired
	protected XMLStorage xml;

	@Autowired
	protected TranscriptManager transcriptManager;

	protected MaterialUnit materialUnit;
	protected Layer<JsonNode> transcript;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		final String nodeId = Objects.firstNonNull((String) getRequest().getAttributes().get("id"), "-1");
		try {
			this.materialUnit = MaterialUnit.forNode(db.getNodeById(Long.parseLong(nodeId)));
			this.transcript = transcriptManager.find(materialUnit);
			if (transcript == null) {
				throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, materialUnit.toString());
			}
		} catch (NumberFormatException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		} catch (ClassCastException e) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST, e);
		} catch (NotFoundException e) {
			throw new ResourceException(Status.CLIENT_ERROR_NOT_FOUND, nodeId);
		} catch (XMLStreamException e) {
			throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, e);
		} catch (IOException e) {
			throw new ResourceException(Status.SERVER_ERROR_SERVICE_UNAVAILABLE, e);
		}
	}

}
