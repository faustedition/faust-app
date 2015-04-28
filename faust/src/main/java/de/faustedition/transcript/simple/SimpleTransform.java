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

package de.faustedition.transcript.simple;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.faustedition.document.MaterialUnit;
import de.faustedition.json.CompactTextModule;
import de.faustedition.transcript.input.HandsXMLTransformerModule;
import de.faustedition.transcript.input.StageXMLTransformerModule;
import eu.interedition.text.*;
import eu.interedition.text.simple.SimpleLayer;
import eu.interedition.text.simple.SimpleTextRepository;
import eu.interedition.text.xml.XMLTransformer;
import eu.interedition.text.xml.XMLTransformerConfigurationBase;
import eu.interedition.text.xml.XMLTransformerModule;
import eu.interedition.text.xml.module.*;
import org.codehaus.jackson.JsonGenerator;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.map.ObjectMapper;
import org.springframework.ui.ModelMap;
import org.xml.sax.InputSource;

import javax.xml.stream.XMLStreamException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.sax.SAXSource;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.*;

import static de.faustedition.xml.Namespaces.TEI_SIG_GE;
import static eu.interedition.text.TextConstants.TEI_NS;


/**
 * User: moz
 * Date: 27/04/15
 * Time: 9:36 PM
 */
public class SimpleTransform {

	protected static XMLTransformerConfigurationBase<JsonNode> configure(XMLTransformerConfigurationBase<JsonNode> conf, MaterialUnit.Type type) {
		conf.addLineElement(new Name(TEI_NS, "text"));
		conf.addLineElement(new Name(TEI_NS, "div"));
		conf.addLineElement(new Name(TEI_NS, "head"));
		conf.addLineElement(new Name(TEI_NS, "sp"));
		conf.addLineElement(new Name(TEI_NS, "stage"));
		conf.addLineElement(new Name(TEI_NS, "speaker"));
		conf.addLineElement(new Name(TEI_NS, "lg"));
		conf.addLineElement(new Name(TEI_NS, "l"));
		conf.addLineElement(new Name(TEI_NS, "p"));
		conf.addLineElement(new Name(TEI_NS, "ab"));
		conf.addLineElement(new Name(TEI_NS, "line"));
		conf.addLineElement(new Name(TEI_SIG_GE, "document"));

		conf.addContainerElement(new Name(TEI_NS, "text"));
		conf.addContainerElement(new Name(TEI_NS, "div"));
		conf.addContainerElement(new Name(TEI_NS, "lg"));
		conf.addContainerElement(new Name(TEI_NS, "subst"));
		conf.addContainerElement(new Name(TEI_NS, "choice"));
		conf.addContainerElement(new Name(TEI_NS, "zone"));

		conf.exclude(new Name(TEI_NS, "teiHeader"));
		conf.exclude(new Name(TEI_NS, "front"));
		conf.exclude(new Name(TEI_NS, "app"));

		conf.include(new Name(TEI_NS, "lem"));

		final List<XMLTransformerModule<JsonNode>> modules = conf.getModules();
		modules.add(new LineElementXMLTransformerModule<JsonNode>());
		modules.add(new NotableCharacterXMLTransformerModule<JsonNode>());
		modules.add(new TextXMLTransformerModule<JsonNode>());
		modules.add(new DefaultAnnotationXMLTransformerModule<JsonNode>());
		modules.add(new CLIXAnnotationXMLTransformerModule<JsonNode>());

		switch (type) {
			case ARCHIVALDOCUMENT:
			case DOCUMENT:
				modules.add(new StageXMLTransformerModule(conf));
				break;
			case PAGE:
				modules.add(new HandsXMLTransformerModule(conf));
				// modules.add(new FacsimilePathXMLTransformerModule(materialUnit));
				break;
			default: break;
		}

		modules.add(new TEIAwareAnnotationXMLTransformerModule<JsonNode>());

		return conf;
	}


	public static void main (String[] args) throws TransformerException, IOException, XMLStreamException {


		StringWriter writer = new StringWriter();
		InputStream input = System.in;
		simpleTransform(input, writer);

		System.out.println (writer);


	}

	public static void simpleTransform(InputStream xmlInput, StringWriter outputWriter) throws TransformerException, IOException, XMLStreamException {
		final TextRepository<JsonNode> textRepository = new SimpleTextRepository<JsonNode>();

		final StringWriter xmlString = new StringWriter();

		TransformerFactory.newInstance().newTransformer().transform(
				new SAXSource(new InputSource(xmlInput)),
				new StreamResult(xmlString)
		);

		final ObjectMapper objectMapper = new ObjectMapper();
		objectMapper.registerModule(new CompactTextModule());

		final Layer<JsonNode> sourceLayer = textRepository.add(TextConstants.XML_TARGET_NAME, new StringReader(xmlString.toString()), null, Collections.<Anchor<JsonNode>>emptySet());

		final XMLTransformerConfigurationBase<JsonNode> conf = configure(new XMLTransformerConfigurationBase<JsonNode>(textRepository) {
			@Override
			protected Layer<JsonNode> translate(Name name, Map<Name, String> attributes, Set<Anchor<JsonNode>> anchors) {
				return new SimpleLayer<JsonNode>(name, "", objectMapper.valueToTree(attributes), anchors, null);
			}
		}, MaterialUnit.Type.PAGE);

		final SimpleLayer<JsonNode> transcriptLayer = (SimpleLayer<JsonNode>) new XMLTransformer<JsonNode>(conf).transform(sourceLayer);

		final Map<String, Name> names = Maps.newHashMap();
		final ArrayList<Layer<JsonNode>> annotations = Lists.newArrayList();


		for (Layer<JsonNode> annotation : transcriptLayer.getPorts()) {
			final Name name = annotation.getName();
			names.put(Long.toString(name.hashCode()), name);
			annotations.add(annotation);
		}


		final JsonGenerator jg = objectMapper.getJsonFactory().createJsonGenerator(outputWriter);

		jg.writeObject(new ModelMap()
				.addAttribute("text", transcriptLayer)
				.addAttribute("textContent", transcriptLayer.read())
				.addAttribute("names", names)
				.addAttribute("annotations", annotations));
		jg.flush();
	}
}




