package de.faustedition.genesis;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.document.Document;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.search.SearchResource;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.XMLStorage;
import de.faustedition.xml.XMLUtil;
import org.neo4j.graphdb.GraphDatabaseService;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.google.common.base.Objects;

import java.io.IOException;
import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

@Component
public class MacrogeneticRelationManager {


	public static final FaustRelationshipType TEMP_PRE_REL = new FaustRelationshipType("temporally-precedes");
	public static final FaustRelationshipType TEMP_SYN_REL = new FaustRelationshipType("synchronous-with");

	public static final FaustURI MACROGENETIC_URI = new FaustURI(FaustAuthority.XML, "/genesis/");

	@Autowired
	private XMLStorage xml;

	@Autowired
	private GraphDatabaseService db;

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
		return failed;
	};

	private void evaluate(FaustURI source) throws SAXException, IOException {
		if (logger.isDebugEnabled()) {
			logger.debug("Adding macrogenetic relations from " + source);
		}

		for (MGRelationship r: parse(source)) {
			Document from = Document.findByUri(db, r.from);
			if (from == null) {
				logger.warn(r.from + " unknown, but referenced in " + source);
				continue;
			}

			Document to = Document.findByUri(db, r.to);
			if (to == null) {
				logger.warn(r.to + " unknown, but referenced in " + source);
				continue;
			}
			
            logger.debug("Adding: " + r.from + " ---" + r.type.name() + "---> " + r.to);
            from.node.createRelationshipTo(to.node, r.type);
		}
	}

	private class MGRelationship {
		public FaustURI from, to;
		public FaustRelationshipType type;	
		public MGRelationship (FaustURI from, FaustURI to, FaustRelationshipType type) {
			this.from = from;
			this.to = to;
			this.type = type;
		}
	}

	public Iterable<MGRelationship> parse(final FaustURI source) throws SAXException, IOException {


		final Set<MGRelationship> relationships = new HashSet<MGRelationship>();
		XMLUtil.saxParser().parse(xml.getInputSource(source), new DefaultHandler() {

			private MGRelationship relationship = null;

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes)
			throws SAXException {

				if 	("relation".equals(localName) && Namespaces.FAUST_NS_URI.equals(uri)) {
					FaustRelationshipType type;
					if("temp-pre".equals(attributes.getValue("name"))) {
						type = TEMP_PRE_REL;
					} else if("temp-syn".equals(attributes.getValue("name"))) {
						type = TEMP_SYN_REL;
					}  else {
						throw new SAXException("The relation " + attributes.getValue("name") + " is unknown.");
					}
					this.relationship = new MGRelationship(null, null, type);
				} else if ("item".equals(localName) && Namespaces.FAUST_NS_URI.equals(uri)) {
					String itemURI = attributes.getValue("uri");
					if (relationship != null) {
						try {
							if (relationship.from == null) {
								relationship.from = new FaustURI(new URI(itemURI));
							} else {
								relationship.to = new FaustURI(new URI(itemURI));
								logger.debug("Parsed: " + relationship.from + " ---" + relationship.type.name() + "---> " + relationship.to);
								relationships.add(relationship);
								relationship = new MGRelationship(new FaustURI(new URI(itemURI)), null, relationship.type);						
							}	
						} catch (Exception e) {
							logger.warn("Invalid URI '" + itemURI);
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

		return relationships;
	}


}



