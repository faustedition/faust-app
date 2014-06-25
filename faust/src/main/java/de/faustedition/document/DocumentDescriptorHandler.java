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

import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;
import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.document.MaterialUnit.Type;
import de.faustedition.graph.FaustGraph;
import de.faustedition.transcript.TranscriptType;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.XMLBaseTracker;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import java.io.IOException;
import java.util.*;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
@Component
@Scope(BeanDefinition.SCOPE_PROTOTYPE)
public class DocumentDescriptorHandler extends DefaultHandler {

	private static final Logger LOG = LoggerFactory.getLogger(DocumentDescriptorHandler.class);
	public static final FaustURI noneURI = new FaustURI(FaustAuthority.SELF, "/none/");
	public static final String internalKeyDocumentSource = "document-source";

	@Autowired
	private FaustGraph graph;

	@Autowired
	private GraphDatabaseService db;

	@Autowired
	private XMLStorage xml;

	private FaustURI source;
	private XMLBaseTracker baseTracker;
	private MaterialUnitCollection materialUnitCollection;

	private Document document;
	private Deque<MaterialUnit> materialUnitStack;
	private int materialUnitCounter;
	private boolean inMetadataSection;
	private Map<String, List<String>> metadata;
	private String metadataKey;
	private StringBuilder metadataValue;

	private static final Set<String> materialUnitNames = ImmutableSet.of("archivalDocument",
			"sheet", "leaf", "disjunctLeaf", "page", "patch", "patchSurface");

	private static final Map<String, String> legacyNames;
	static {
		legacyNames = new HashMap<String, String>();
		legacyNames.put("idno", "callnumber");
		legacyNames.put("repository", "archive");
	}
	
	private static final Map<String, String> valueAttribute;
	static {
		valueAttribute = new HashMap<String, String>();
		valueAttribute.put("textTranscript", "uri");
		valueAttribute.put("docTranscript", "uri");
	}


	public void handle(FaustURI source) throws IOException, SAXException {
		this.source = source;
		this.baseTracker = new XMLBaseTracker(source.toString());
		this.materialUnitCollection = graph.getMaterialUnits();

		final InputSource xmlSource = xml.getInputSource(source);
		try {
			XMLUtil.saxParser().parse(xmlSource, this);
			if (document != null) {
				document.index();
			}
			if (LOG.isDebugEnabled()) {
				LOG.debug("Read " + source + " into " + document + "[" +  document.node.getId() + "]");
			}
		} finally {
			xmlSource.getByteStream().close();
		}

	}

	@Override
	public void startDocument() throws SAXException {
		document = null;
		materialUnitStack = new ArrayDeque<MaterialUnit>();
		materialUnitCounter = 0;
		inMetadataSection = false;
		metadata = null;
		metadataKey = null;
		metadataValue = null;
	}

	
	@Override
	public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
		baseTracker.startElement(uri, localName, qName, attributes);

		if (!Namespaces.FAUST_NS_URI.equals(uri)) {
			return;
		}

		
		if (materialUnitNames.contains(localName)) {
			try {
				
				
				final MaterialUnit.Type type = MaterialUnit.Type.valueOf(localName.toUpperCase());
				MaterialUnit unit;
				switch (type) {
					case DOCUMENT:
					case ARCHIVALDOCUMENT:
						unit = new Document(db.createNode(), type, source);
						break;
					default:
						unit = new MaterialUnit(db.createNode(), type);
				}


				unit.setOrder(materialUnitCounter++);

				TranscriptType transcriptType = TranscriptType.DOCUMENTARY;
				if (materialUnitStack.isEmpty()) {
					if (!(unit instanceof Document)) {
						throw new SAXException("Encountered top-level material unit of wrong @type '"+ Type.DOCUMENT + "'");
					}
					document = (Document) unit;
					transcriptType = TranscriptType.TEXTUAL;
				} else {
					materialUnitStack.peek().add(unit);
				}
				materialUnitStack.push(unit);
				materialUnitCollection.add(unit);

			} catch (IllegalArgumentException e) {
				throw new SAXException("Encountered invalid @type or @transcript in <" + localName + "/>", e);
			}
		} else if ("metadata".equals(localName) && !materialUnitStack.isEmpty()) {
			inMetadataSection = true;
			metadata = new HashMap<String, List<String>>();
            metadata.put(internalKeyDocumentSource, ImmutableList.<String>of(this.source.toString()));
		} else if (inMetadataSection && metadataKey == null) {
			// String type = attributes.getValue("type");
			// metadataKey = type == null ? localName : localName + "_" + type;
			
			metadataKey = metadataKeyFromElement(localName, attributes);
			
			metadataValue = new StringBuilder();
			if (valueAttribute.containsKey(localName)) {
				String attributeVal = attributes.getValue(valueAttribute.get(localName));
				metadataValue.append(attributeVal != null ? attributeVal : "none");
			}
			
			// TODO: transcript uris as regular metadata
			
			if ("textTranscript".equals(localName) || "docTranscript".equals(localName)) {
				TranscriptType type = localName == "textTranscript" ? TranscriptType.TEXTUAL : 
					TranscriptType.DOCUMENTARY;
				final String transcript = metadataValue.toString();
				if (transcript != null) {
					final MaterialUnit unit = materialUnitStack.peek();
					final FaustURI transcriptSource;
					if (!transcript.equals("none")) {
						transcriptSource = new FaustURI(baseTracker.getBaseURI().resolve(transcript));
					} else {
						transcriptSource = noneURI;
					}
					unit.setTranscriptSource(transcriptSource);
				}
			}

		}
	}

	@Override
	public void endElement(String uri, String localName, String qName) throws SAXException {
		baseTracker.endElement(uri, localName, qName);
		if (!Namespaces.FAUST_NS_URI.equals(uri)) {
			return;
		}

		if (materialUnitNames.contains(localName)) {
			materialUnitStack.pop();
		} else if ("metadata".equals(localName) && !materialUnitStack.isEmpty()) {
			final MaterialUnit subject = materialUnitStack.peek();
			calculateMetadata(subject);
			for (Map.Entry<String, List<String>> metadataEntry : metadata.entrySet()) {
				List<String> value = metadataEntry.getValue();
				subject.setMetadata(convertMetadataKey(metadataEntry.getKey()),
					value.toArray(new String[value.size()]));
			}
			metadata = null;
			inMetadataSection = false;

			if (subject instanceof Document) {
				final String archiveId = subject.getMetadataValue("archive");
				if (archiveId != null) {
					final Archive archive = graph.getArchives().findById(archiveId);
					if (archive == null) {
						throw new SAXException("Invalid archive reference: " + archiveId);
					}
					archive.add(subject);
				}
			}
		} else if (inMetadataSection && metadataKey != null) {
			if (metadata.containsKey(metadataKey)) {
				metadata.get(metadataKey).add(metadataValue.toString());
			} else {
				List<String> values = new ArrayList<String>();
				values.add(metadataValue.toString());
				metadata.put(metadataKey, values);
			}
			metadataKey = null;
			metadataValue = null;
		}
	}

	private void calculateMetadata(MaterialUnit subject) {
		if (subject instanceof Document) {
			
			List<String> repositories = metadata.get("archive");
			if (repositories == null || repositories.size() != 1) { 
				throw new DocumentDescriptorInvalidException("Document descriptor for " + this.source + " invalid: Document must be in exactly one repository.");
			} else {
				HashSet<String> metadataKeys = Sets.newHashSet(metadata.keySet());
				for(String key : metadataKeys) {
					if (key.startsWith("callnumber." + repositories.get(0))) {
						metadata.put("callnumber", Arrays.asList(metadata.get(key).get(0)));
						// TODO: check number of idno type
					}
				}
			}
		}
	}

	protected String metadataKeyFromElement(String localName, Attributes attributes) {
		
		String key = legacyNames.containsKey(localName) ? legacyNames.get(localName) : localName;
		
		String type = attributes.getValue("", "type");
		
		if ("idno".equals(localName) && type != null) 
			return key + "." + type;
		else 
			return key;
	}
	
	protected String convertMetadataKey(String key) {
		final StringBuilder converted = new StringBuilder();
		for (int cc = 0; cc < key.length(); cc++) {
			char current = key.charAt(cc);
			if (cc > 0 && Character.isUpperCase(current)) {
				converted.append("-");
				current = Character.toLowerCase(current);
			}
			if (!Character.isLetterOrDigit(current) && current != '.') {
				current = '-';
			}
			converted.append(current);
		}
		return converted.toString();
	}

	@Override
	public void characters(char[] ch, int start, int length) throws SAXException {
		if (inMetadataSection && metadataKey != null) {
			metadataValue.append(ch, start, length);
		}
	}

	@Override
	public void endDocument() throws SAXException {
		document.getMetadata("");
	}
	
	
}
