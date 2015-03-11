/*
 * Copyright (c) 2015 Faust Edition development team.
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

package de.faustedition.query;

import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.Collections2;
import com.google.common.collect.Iterables;
import de.faustedition.JsonRepresentationFactory;
import de.faustedition.document.Document;
import de.faustedition.document.MaterialUnit;
import de.faustedition.graph.FaustGraph;
import org.restlet.data.Reference;
import org.restlet.data.Status;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ResourceException;
import org.restlet.resource.ServerResource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import javax.annotation.Nullable;
import java.util.Arrays;
import java.util.Collection;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class QueryResource extends ServerResource {

	private static final Logger LOG = LoggerFactory.getLogger(QueryResource.class);

	@Autowired
	private JsonRepresentationFactory jsonFactory;

	@Autowired
	private FaustGraph graph;

	private enum QueryTerms {ALLDOCUMENTS};

	private String queryTerm;

	@Override
	protected void doInit() throws ResourceException {
		super.doInit();
		queryTerm = Reference.decode(Objects.firstNonNull((String) getRequest().getAttributes().get("term"), ""));
		if (queryTerm.isEmpty()) {
			throw new ResourceException(Status.CLIENT_ERROR_BAD_REQUEST);
		}
	}

	@Get("json")
	public Representation results() {

		if (LOG.isTraceEnabled()) {
			LOG.trace("Searching for '{}'", queryTerm);
		}

		if (QueryTerms.ALLDOCUMENTS.name().equals(queryTerm.toUpperCase())) {

			Iterable<Document> allDocuments = Iterables.filter(graph.getMaterialUnits(), Document.class);

			Iterable<String> allMaterialUnits = Iterables.<Document, String>transform (allDocuments,

					new Function<Document, String>() {
						@Override
						public String apply(@Nullable Document input) {

							return Iterables.getFirst(Arrays.asList(input.getMetadata("uri")), "none");
						}
					});

			return jsonFactory.map(allMaterialUnits);

		} else return null;

	}
}
