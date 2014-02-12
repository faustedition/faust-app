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

package de.faustedition.document;

import de.faustedition.FaustURI;
import de.faustedition.JsonRespresentation;
import de.faustedition.template.TemplateRepresentationFactory;
import org.restlet.data.CharacterSet;
import org.restlet.data.Language;
import org.restlet.data.MediaType;
import org.restlet.representation.Representation;
import org.restlet.representation.StringRepresentation;
import org.restlet.resource.Get;
import org.restlet.resource.ServerResource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeSet;

@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DocumentResource extends ServerResource {

	@Autowired
	private TemplateRepresentationFactory viewFactory;

	private Document document;
	
	private FaustURI path;

	public void setDocument(Document document) {
		this.document = document;
	}

	public void setPath(FaustURI path) {
		this.path = path;
	}
	
	@Get("html")
	public Representation overview() throws IOException {
		Map<String, Object> viewModel = new HashMap<String, Object>();
		viewModel.put("document", document);
		viewModel.put("contents", document.getSortedContents());
		viewModel.put("path", path.toString());

		Map<String, String> params = this.getRequest().getResourceRef().getQueryAsForm().getValuesMap();
		if ("transcript-bare".equals(params.get("view"))) {
			return viewFactory.create("document/document-transcript-bare", getRequest().getClientInfo(), viewModel);
		} else {				
			return viewFactory.create("document/document-app", getRequest().getClientInfo(), viewModel);
		}
	}
	
	@Get("svg")
	public Representation graphic() {
		String result = "<xml version=\"1open.0\"?><svg xmlns=\"http://www.w3.org/2000/svg\" version=\"1.1\">"
			+ "<script type=\"text/javascript\" ></script>"
			+ "</svg>";
		
		
		return new StringRepresentation(result, MediaType.IMAGE_SVG,
				Language.DEFAULT, CharacterSet.UTF_8);
	}
	
	@Get("json")
	public Representation documentStructure() {
		return new JsonRespresentation() {

			@Override
			protected void generate() throws IOException {
				generate(document);
			}

			protected void generate(MaterialUnit unit) throws IOException {
				generator.writeStartObject();

				generator.writeStringField("type", unit.getType().name().toLowerCase());
				if (unit instanceof Document) {
					generator.writeStringField("name", unit.toString());

					generator.writeStringField("document-source", unit.getMetadataValue("document-source"));
					generator.writeStringField("note", unit.getMetadataValue("note"));

					generator.writeStringField("callnumber.gsa-1", unit.getMetadataValue("callnumber.gsa-1"));
					generator.writeStringField("callnumber.gsa-2", unit.getMetadataValue("callnumber.gsa-2"));
					generator.writeStringField("callnumber.wa-faust", unit.getMetadataValue("callnumber.wa-faust"));

				}
				generator.writeNumberField("order", unit.getOrder());
				generator.writeNumberField("id", unit.node.getId());


//				final GoddagTranscript transcript = unit.getTranscript();
				if (unit.getTranscriptSource() != null) {
					generator.writeObjectFieldStart("transcript");
//					generator.writeStringField("type", transcript.getType().name().toLowerCase());
					generator.writeStringField("source", unit.getTranscriptSource().toString());
//					if (transcript.getType() == TranscriptType.DOCUMENTARY) {
//						DocumentaryGoddagTranscript dt = (DocumentaryGoddagTranscript) transcript;
//						generator.writeArrayFieldStart("facsimiles");
//						for (FaustURI facsimile : dt.getFacsimileReferences()) {
//							generator.writeString(facsimile.toString());
//						}
//						generator.writeEndArray();
//					}
					generator.writeEndObject();
					final FaustURI facsimile = unit.getFacsimile();
					if (facsimile != null) 
							generator.writeStringField("facsimile", facsimile.toString());
				}
				generator.writeArrayFieldStart("contents");
				for (MaterialUnit content : new TreeSet<MaterialUnit>(unit)) {
					generate(content);
				}
				generator.writeEndArray();
				generator.writeEndObject();
			}
		};
	}

	
}
