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

package de.faustedition.text;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.restlet.representation.Representation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import de.faustedition.template.TemplateRepresentationFactory;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TextResource extends ServerResource {

	@Autowired
	private TemplateRepresentationFactory viewFactory;

	private Text text;

	public void setText(Text text) {
		this.text = text;
	}

	@Get("html")
	public Representation page() throws IOException {
		Map<String, Object> model = new HashMap<String, Object>();
		model.put("text", text);
		return viewFactory.create("text/text", getClientInfo(), model);
	}
}
