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

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.faustedition.JsonRepresentationFactory;
import eu.interedition.text.*;
import org.codehaus.jackson.JsonNode;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.ui.ModelMap;
import org.xml.sax.SAXException;

import javax.xml.stream.XMLStreamException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;

import static eu.interedition.text.Query.text;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class TranscriptSourceResource extends TranscriptResource {

	@Autowired
	private JsonRepresentationFactory jsonFactory;
	
	@Autowired
	private TextRepository<JsonNode> textRepo;
	
	@Get("xml")
	public Representation source() throws IOException, XMLStreamException, SAXException {
    for (Anchor<JsonNode> anchor : transcript.getAnchors()) {
      final Layer<JsonNode> text = anchor.getText();
      if (TextConstants.XML_SOURCE_NAME.equals(text.getName())) {
        return new StringRepresentation(text.read());
      }
    }
		return null;
	}

	@Get("txt")
	public Representation plainText() throws IOException {
		return new StringRepresentation(transcript.read());
	}

	//@Get("json")
	public Representation model() throws IOException {
		final Map<String, Name> names = Maps.newHashMap();
		final ArrayList<Layer<JsonNode>> annotations = Lists.newArrayList();
		for (Layer<JsonNode> annotation : textRepo.query(text(transcript))) {
			final Name name = annotation.getName();
			names.put(Long.toString(name.hashCode()), name);
			annotations.add(annotation);
		}
		return jsonFactory.map(new ModelMap()
			.addAttribute("text", transcript)
			.addAttribute("textContent", transcript.read())
			.addAttribute("names", names)
			.addAttribute("annotations", annotations));
	}

	@Get("json")
	public Representation compactModel() throws IOException {
		final Map<String, Name> names = Maps.newHashMap();
		final ArrayList<Layer<JsonNode>> annotations = Lists.newArrayList();
		for (Layer<JsonNode> annotation : textRepo.query(text(transcript))) {
			final Name name = annotation.getName();
			names.put(Long.toString(name.hashCode()), name);
			annotations.add(annotation);
		}
		
		return jsonFactory.map(new ModelMap()
			.addAttribute("text", transcript)
			.addAttribute("textContent", transcript.read())
			.addAttribute("names", names)
			.addAttribute("annotations", annotations));
	}

}
