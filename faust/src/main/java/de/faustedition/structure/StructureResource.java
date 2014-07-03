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

package de.faustedition.structure;

import de.faustedition.FaustURI;
import de.faustedition.template.TemplateRepresentationFactory;
import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;


@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class StructureResource extends ServerResource {

	@Autowired
	private TemplateRepresentationFactory viewFactory;

	private FaustURI uri;

	public void setURI(FaustURI uri) {
		this.uri = uri;
	}

	@Get("html")
	public Representation overview() throws IOException {
		Map<String, Object> viewModel = new HashMap<String, Object>();
		viewModel.put("uri", uri);
		
		return viewFactory.create("structure/structure", getRequest().getClientInfo(), viewModel);
	}


}
