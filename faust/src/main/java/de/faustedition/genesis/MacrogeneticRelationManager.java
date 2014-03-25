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

package de.faustedition.genesis;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.document.Document;
import de.faustedition.graph.FaustGraph;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;

@Component
public class MacrogeneticRelationManager {


	public static final FaustRelationshipType TEMP_PRE_REL = new FaustRelationshipType("temporally-precedes");
	public static final FaustRelationshipType TEMP_SYN_REL = new FaustRelationshipType("synchronous-with");

	public static final FaustURI MACROGENETIC_URI = new FaustURI(FaustAuthority.XML, "/genesis/");
	
	public static final String SOURCE_PROPERTY = "source";
	@Autowired
	private XMLStorage xml;

	@Autowired
	private GraphDatabaseService db;
	
	@Autowired
	private FaustGraph faustGraph;

	@Autowired
	private Logger logger;

    @Transactional
	public Set<FaustURI> feedGraph() {
		logger.info("Feeding macrogenetic data into graph");
		final Set<FaustURI> failed = new HashSet<FaustURI>();
		for (final FaustURI macrogeneticFile: xml.iterate(new FaustURI(FaustAuthority.XML, "/macrogenesis"))) {
            try {
                logger.debug("Parsing macrogenetic file " + macrogeneticFile);
                evaluate(macrogeneticFile);
            } catch (SAXException e) {
                logger.error("XML error while adding macrogenetic file " + macrogeneticFile, e);
                failed.add(macrogeneticFile);
            } catch (IOException e) {
                logger.error("I/O error while adding macrogenetic file " + macrogeneticFile, e);
                failed.add(macrogeneticFile);
            }
		}
		logger.debug("Found genetic sources: ");
		for (GeneticSource gs :faustGraph.getGeneticSources())
			logger.debug(gs.getUri().toString());
		return failed;
	};

	private void evaluate(FaustURI source) throws SAXException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding macrogenetic relations from " + source);
		}

		parse(source);
            
		
	}
	
	private void setRelationship(MGRelationship r, FaustURI source, FaustURI geneticSource) {
		Document from = Document.findByUri(db, r.from);
		if (from == null) {
			logger.warn(r.from + " unknown, but referenced in " + source);
			return;
		}

		Document to = Document.findByUri(db, r.to);
		if (to == null) {
			logger.warn(r.to + " unknown, but referenced in " + source);
			return;
		}
		
        logger.debug("Adding: " + r.from + " ---" + r.type.name() + "---> " + r.to);
        try {
        	Relationship rel =  from.node.createRelationshipTo(to.node, r.type);
        	rel.setProperty(SOURCE_PROPERTY, source.toString());
        	rel.setProperty(Document.GENETIC_SOURCE_PROPERTY, geneticSource.toString());
        } catch (Exception e) {
        	logger.warn("Could not create relationship " + r.from + " ---" + r.type.name() + "---> " + r.to + ". Reason: " + e.getMessage());
        }
	}

	private class MGRelationship {
		public FaustURI from, to, source;
		public FaustRelationshipType type;
		public MGRelationship (FaustURI from, FaustURI to, FaustRelationshipType type) {
			this.from = from;
			this.to = to;
			this.type = type;
		}
	}

	public void parse(final FaustURI source) throws SAXException, IOException {


		//final Set<MGRelationship> relationships = new HashSet<MGRelationship>();
		XMLUtil.saxParser().parse(xml.getInputSource(source), new DefaultHandler() {

			private MGRelationship relationship = null;
			private GeneticSourceCollection geneticSources = faustGraph.getGeneticSources();
			private ArrayList<FaustURI> geneticSourceURIs = new ArrayList<FaustURI>();
			

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {

				if 	("relation".equals(localName) && Namespaces.FAUST_NS_URI.equals(uri)) {
					geneticSourceURIs.clear();
					FaustRelationshipType type;
					if("temp-pre".equals(attributes.getValue("name"))) {
						type = TEMP_PRE_REL;
					} else if("temp-syn".equals(attributes.getValue("name"))) {
						type = TEMP_SYN_REL;
					}  else {
						logger.warn("The relation " + attributes.getValue("name") + " is unknown.");
						type = null;
					}
					this.relationship = new MGRelationship(null, null, type);
				} else if ("item".equals(localName) && Namespaces.FAUST_NS_URI.equals(uri)) {
					String itemURI = attributes.getValue("uri");
					if (relationship != null) {
						
							if (relationship.from == null) {
								try {
									relationship.from = new FaustURI(new URI(itemURI));
								} catch (URISyntaxException e) {
									logger.warn("Invalid URI '" + itemURI);
								} catch (IllegalArgumentException e) {
									logger.warn("Invalid Faust URI " + itemURI);
								} 
								
							} else {
								
								try {
									relationship.to = new FaustURI(new URI(itemURI));
								} catch (URISyntaxException e) {
									logger.warn("Invalid URI '" + itemURI);
								} catch (IllegalArgumentException e) {
									logger.warn("Invalid Faust URI " + itemURI);
								}

								if (relationship.to != null && relationship.type != null) {
									logger.debug("Parsed: " + relationship.from + " ---" + relationship.type.name() + "---> " + relationship.to);

//									for (FaustURI geneticSourceURI: geneticSourceURIs) {
//										setRelationship(relationship, source, geneticSourceURI);
//									}
									if (geneticSourceURIs.size() > 0)
										setRelationship(relationship, source, geneticSourceURIs.get(0));
										
									//relationships.add(relationship);
									relationship = new MGRelationship(relationship.to, null, relationship.type);
								}
								

							}	
						}
				} else if ("source".equals(localName) && Namespaces.FAUST_NS_URI.equals(uri)) {
					String sourceURI = attributes.getValue("uri");
					if (sourceURI != null) {
						try {
							boolean registered = false;
							for (GeneticSource geneticSource : geneticSources) {
								String registeredURI = geneticSource.getUri().toString();
								if (registeredURI.equals(sourceURI)) {
									registered = true;
									break;
								}
							}
							if (!registered)
								geneticSources.add(new GeneticSource(db, new FaustURI(new URI(sourceURI))));
							geneticSourceURIs.add(new FaustURI(new URI(sourceURI)));
						} catch (URISyntaxException e) {
							logger.warn("Invalid URI '" + sourceURI);
						} catch (IllegalArgumentException e) {
							logger.warn("Invalid Faust URI " + sourceURI);
						}
					}
				}	

			}

			@Override
			public void endElement(String uri, String localName, String qName)
			throws SAXException {
				if ("relation".equals(localName) && Namespaces.FAUST_NS_URI.equals(uri)) {
					this.relationship = null;
				}				
			}
		});

	}


}



