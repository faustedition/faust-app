package de.faustedition.transcript;

import static de.faustedition.xml.CustomNamespaceMap.FAUST_NS_PREFIX;
import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_PREFIX;

import java.util.HashSet;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import org.juxtasoftware.goddag.Element;
import org.juxtasoftware.goddag.Text;
import org.juxtasoftware.goddag.util.DefaultGoddagEventHandler;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;

import de.faustedition.FaustURI;
import de.faustedition.xml.CustomNamespaceMap;

public class DocumentaryTranscript extends Transcript {

    public DocumentaryTranscript(Node node) {
        super(node);
    }

    public DocumentaryTranscript(Node node, FaustURI source, SortedSet<FaustURI> facsimileReferences) {
        super(node, Type.DOCUMENTARY, source);
        setFacsimileReferences(facsimileReferences);
    }

    public void setFacsimileReferences(SortedSet<FaustURI> facsimileReferences) {
        String[] uris = new String[facsimileReferences.size()];
        int uc = 0;
        for (FaustURI uri : facsimileReferences) {
            uris[uc++] = uri.toString();
        }
        getUnderlyingNode().setProperty(PREFIX + ".documentary.facsimiles", uris);
    }

    public SortedSet<FaustURI> getFacsimileReferences() {
        SortedSet<FaustURI> facsimileReferences = new TreeSet<FaustURI>();
        for (String uri : ((String[]) getUnderlyingNode().getProperty(PREFIX + ".documentary.facsimiles"))) {
            facsimileReferences.add(FaustURI.parse(uri));
        }
        return facsimileReferences;
    }

    public void postprocess() {
        final Element documentRoot = getRoot(CustomNamespaceMap.TEI_SIG_GE_PREFIX, "document");
        if (documentRoot == null) {
            return;
        }

        final GraphDatabaseService db = getUnderlyingNode().getGraphDatabase();
        documentRoot.stream(documentRoot, new DefaultGoddagEventHandler() {
            Element hand = null;
            String handId = null;
            Element hands = getRoot(FAUST_NS_PREFIX, "hands");
            Set<Element> handShifts = new HashSet<Element>();

            @Override
            public void text(Text text) {
                if (hand != null) {
                    hand.insert(hands, text, null);
                }
            }

            @Override
            public void startElement(Element element) {
                if ("handShift".equals(element.getName()) && TEI_NS_PREFIX.equals(element.getPrefix())) {
                    handShifts.add(element);
                    String newHandId = element.getAttributeValue(TEI_NS_PREFIX, "new");
                    if (newHandId != null) {
                        newHandId = newHandId.replaceAll("^#", "");
                        if (handId == null || !newHandId.equals(handId)) {
                            insertHand();
                            handId = newHandId;
                            hand = Element.create(db, FAUST_NS_PREFIX, "hand");
                            hand.setAttribute(FAUST_NS_PREFIX, "id", handId);
                        }
                    }
                }
            }

            @Override
            public void endElement(Element element) {
                if (element.equals(documentRoot)) {
                    insertHand();
                    for (Element handShift : handShifts) {
                        handShift.delete(documentRoot);
                    }
                }
            }

            protected void insertHand() {
                if (hand != null && hand.hasChildren(hands)) {
                    hands.insert(hands, hand, null);
                }
            }
        });
    }
}
