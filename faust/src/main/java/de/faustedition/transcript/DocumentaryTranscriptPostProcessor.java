package de.faustedition.transcript;

import static de.faustedition.xml.CustomNamespaceMap.FAUST_NS_PREFIX;
import static de.faustedition.xml.CustomNamespaceMap.TEI_NS_PREFIX;

import java.util.HashSet;
import java.util.Set;

import org.juxtasoftware.goddag.Element;
import org.juxtasoftware.goddag.Text;
import org.juxtasoftware.goddag.util.DefaultGoddagEventHandler;
import org.juxtasoftware.goddag.util.MultiplexingGoddagEventHandler;
import org.neo4j.graphdb.GraphDatabaseService;

import de.faustedition.xml.CustomNamespaceMap;

public class DocumentaryTranscriptPostProcessor extends MultiplexingGoddagEventHandler implements Runnable {

    private final DocumentaryTranscript transcript;
    private final Element source;
    private GraphDatabaseService db;

    public DocumentaryTranscriptPostProcessor(DocumentaryTranscript transcript) {
        super();
        this.transcript = transcript;
        this.source = transcript.getRoot(CustomNamespaceMap.TEI_SIG_GE_PREFIX, "document");
        this.db = transcript.getUnderlyingNode().getGraphDatabase();
        setHandlers(new HandViewProcessor());
    }

    @Override
    public void run() {
        source.stream(source, this);
    }

    private class HandViewProcessor extends DefaultGoddagEventHandler {
        Element hand = null;
        String handId = null;
        Element hands = transcript.getRoot(FAUST_NS_PREFIX, "hands");
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
            if (element.equals(source)) {
                insertHand();
                for (Element handShift : handShifts) {
                    handShift.delete(source);
                }
            }
        }

        protected void insertHand() {
            if (hand != null && hand.hasChildren(hands)) {
                hands.insert(hands, hand, null);
            }
        }
    }
}
