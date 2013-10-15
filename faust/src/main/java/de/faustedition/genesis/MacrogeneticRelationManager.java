package de.faustedition.genesis;

import de.faustedition.FaustAuthority;
import de.faustedition.FaustURI;
import de.faustedition.document.Document;
import de.faustedition.graph.FaustRelationshipType;
import de.faustedition.graph.Graph;
import de.faustedition.xml.Namespaces;
import de.faustedition.xml.Sources;
import de.faustedition.xml.XMLUtil;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Relationship;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.inject.Inject;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

public class MacrogeneticRelationManager {


    public static final FaustRelationshipType TEMP_PRE_REL = new FaustRelationshipType("temporally-precedes");
    public static final FaustRelationshipType TEMP_SYN_REL = new FaustRelationshipType("synchronous-with");

    public static final FaustURI MACROGENETIC_URI = new FaustURI(FaustAuthority.XML, "/genesis/");

    public static final String SOURCE_PROPERTY = "source";

    private final Sources xml;
    private final GraphDatabaseService db;
    private final Logger logger;

    @Inject
    public MacrogeneticRelationManager(Sources xml, GraphDatabaseService db, Logger logger) {
        this.xml = xml;
        this.db = db;
        this.logger = logger;
    }

    public Set<FaustURI> feedGraph(Graph graph) {
        logger.info("Feeding macrogenetic data into graph");
        final Set<FaustURI> failed = new HashSet<FaustURI>();
        for (final FaustURI macrogeneticFile : xml.iterate(new FaustURI(FaustAuthority.XML, "/macrogenesis"))) {
            try {
                logger.fine("Parsing macrogenetic file " + macrogeneticFile);
                logger.fine("Adding macrogenetic relations from " + macrogeneticFile);
                parse(macrogeneticFile, graph);
            } catch (SAXException e) {
                logger.log(Level.SEVERE, "XML error while adding macrogenetic file " + macrogeneticFile, e);
                failed.add(macrogeneticFile);
            } catch (IOException e) {
                logger.log(Level.SEVERE, "I/O error while adding macrogenetic file " + macrogeneticFile, e);
                failed.add(macrogeneticFile);
            }
        }
        logger.fine("Found genetic sources: ");
        for (GeneticSource gs : graph.getGeneticSources())
            logger.fine(gs.getUri().toString());
        return failed;
    }

    ;

    private void setRelationship(MGRelationship r, FaustURI source, FaustURI geneticSource) {
        Document from = Document.findByUri(db, r.from);
        if (from == null) {
            logger.warning(r.from + " unknown, but referenced in " + source);
            return;
        }

        Document to = Document.findByUri(db, r.to);
        if (to == null) {
            logger.warning(r.to + " unknown, but referenced in " + source);
            return;
        }

        logger.fine("Adding: " + r.from + " ---" + r.type.name() + "---> " + r.to);
        try {
            Relationship rel = from.node.createRelationshipTo(to.node, r.type);
            rel.setProperty(SOURCE_PROPERTY, source.toString());
            rel.setProperty(Document.GENETIC_SOURCE_PROPERTY, geneticSource.toString());
        } catch (Exception e) {
            logger.warning("Could not create relationship " + r.from + " ---" + r.type.name() + "---> " + r.to + ". Reason: " + e.getMessage());
        }
    }

    private class MGRelationship {
        public FaustURI from, to, source;
        public FaustRelationshipType type;

        public MGRelationship(FaustURI from, FaustURI to, FaustRelationshipType type) {
            this.from = from;
            this.to = to;
            this.type = type;
        }
    }

    public void parse(final FaustURI source, final Graph graph) throws SAXException, IOException {


        //final Set<MGRelationship> relationships = new HashSet<MGRelationship>();
        XMLUtil.saxParser().parse(xml.getInputSource(source), new DefaultHandler() {

            private MGRelationship relationship = null;
            private GeneticSourceCollection geneticSources = graph.getGeneticSources();
            private ArrayList<FaustURI> geneticSourceURIs = new ArrayList<FaustURI>();


            @Override
            public void startElement(String uri, String localName, String qName, Attributes attributes)
                    throws SAXException {

                if ("relation".equals(localName) && Namespaces.FAUST_NS_URI.equals(uri)) {
                    geneticSourceURIs.clear();
                    FaustRelationshipType type;
                    if ("temp-pre".equals(attributes.getValue("name"))) {
                        type = TEMP_PRE_REL;
                    } else if ("temp-syn".equals(attributes.getValue("name"))) {
                        type = TEMP_SYN_REL;
                    } else {
                        logger.warning("The relation " + attributes.getValue("name") + " is unknown.");
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
                                logger.warning("Invalid URI '" + itemURI);
                            } catch (IllegalArgumentException e) {
                                logger.warning("Invalid Faust URI " + itemURI);
                            }

                        } else {

                            try {
                                relationship.to = new FaustURI(new URI(itemURI));
                            } catch (URISyntaxException e) {
                                logger.warning("Invalid URI '" + itemURI);
                            } catch (IllegalArgumentException e) {
                                logger.warning("Invalid Faust URI " + itemURI);
                            }

                            if (relationship.to != null && relationship.type != null) {
                                logger.fine("Parsed: " + relationship.from + " ---" + relationship.type.name() + "---> " + relationship.to);

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
                            logger.warning("Invalid URI '" + sourceURI);
                        } catch (IllegalArgumentException e) {
                            logger.warning("Invalid Faust URI " + sourceURI);
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



