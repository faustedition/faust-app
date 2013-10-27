package de.faustedition.transcript;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Strings;
import com.google.common.collect.ForwardingIterator;
import com.google.common.collect.Lists;
import de.faustedition.text.TextAnnotationEnd;
import de.faustedition.text.TextAnnotationStart;
import de.faustedition.text.NamespaceMapping;
import de.faustedition.text.TextContent;
import de.faustedition.text.TextToken;
import de.faustedition.text.XML;
import de.faustedition.text.XMLEvent2TextToken;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Queue;
import java.util.logging.Level;
import java.util.logging.Logger;

import static de.faustedition.text.NamespaceMapping.FAUST_NS_URI;
import static de.faustedition.text.NamespaceMapping.TEI_NS_URI;
import static de.faustedition.text.NamespaceMapping.TEI_SIG_GE_URI;
import static de.faustedition.text.NamespaceMapping.map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranscriptMarkupHandler extends ForwardingIterator<TextToken> {

    public static final String GAP_CHAR = "_"; //"\u00d7";
    public static final String IMPRECISE_GAP_CHAR = "_"; //"\u00d7";

    public static final String INSERT_RIGHT_CHAR = "\u2308";
    public static final String INSERT_LEFT_CHAR = "\u2309";

    private static final Logger LOG = Logger.getLogger(TranscriptMarkupHandler.class.getName());

    private final Iterator<TextToken> delegate;
    private final ObjectMapper objectMapper;
    private final Queue<TextToken> buf = Lists.newLinkedList();
    private final Iterator<String> ids = XMLEvent2TextToken.ids("transcript_");

    private final String insertKey;
    private final String insertOrientationKey;

    private final String gapKey;
    private final String gapQuantityKey;
    private final String gapPrecisionKey;
    private final String gapAtLeastKey;

    private final String stageAttributeName;
    private final String stageKey;

    private final String handShiftName;
    private final String handKey;
    private final String newAttributeName;
    private final String xmlNameKey;
    private final String documentKey;

    private String stageAnnotationId = null;
    private String handAnnotationId = null;
    private LinkedList<String> insertAnnotationIds = Lists.newLinkedList();

    public TranscriptMarkupHandler(Iterator<TextToken> delegate, ObjectMapper objectMapper, NamespaceMapping namespaceMapping) {
        this.delegate = delegate;
        this.objectMapper = objectMapper;

        this.xmlNameKey = map(namespaceMapping, XML.XML_ELEMENT_NAME);

        this.insertKey = map(namespaceMapping, new QName(FAUST_NS_URI, "ins"));
        this.insertOrientationKey = map(namespaceMapping, new QName(FAUST_NS_URI, "orient"));

        this.gapKey = map(namespaceMapping, new QName(TEI_NS_URI, "gap"));
        this.gapQuantityKey = map(namespaceMapping, new QName(TEI_NS_URI, "quantity"));
        this.gapPrecisionKey = map(namespaceMapping, new QName(TEI_NS_URI, "precision"));
        this.gapAtLeastKey = map(namespaceMapping, new QName(TEI_NS_URI, "atLeast"));

        this.stageKey = map(namespaceMapping, new QName(FAUST_NS_URI, "stage"));
        this.stageAttributeName = map(namespaceMapping, new QName(TEI_SIG_GE_URI, "stage"));

        this.handKey = map(namespaceMapping, new QName(FAUST_NS_URI, "hand"));
        this.handShiftName = map(namespaceMapping, new QName(TEI_NS_URI, "handShift"));
        this.newAttributeName = map(namespaceMapping, new QName(TEI_NS_URI, "new"));

        this.documentKey = map(namespaceMapping, new QName(TEI_SIG_GE_URI, "document"));
    }

    @Override
    protected Iterator<TextToken> delegate() {
        return delegate;
    }

    @Override
    public TextToken next() {
        return buf.remove();
    }

    @Override
    public boolean hasNext() {
        if (buf.isEmpty() && super.hasNext()) {
            final TextToken next = super.next();
            boolean tokenPending = true;
            if (next instanceof TextAnnotationStart) {
                final TextAnnotationStart annotationStart = (TextAnnotationStart) next;
                final ObjectNode data = annotationStart.getData();

                final String xmlName = data.path(xmlNameKey).asText();
                if (xmlName.equals(documentKey)) {
                    handEnd();
                    stageEnd();
                } else if (xmlName.equals(gapKey)) {
                    buf.add(next);
                    tokenPending = false;
                    try {
                        final String quantity = data.path(gapQuantityKey).asText();
                        final String atLeast = data.path(gapAtLeastKey).asText();
                        final String precision = data.path(gapPrecisionKey).asText();

                        final boolean precise = (atLeast.isEmpty() && (precision.isEmpty() || !precision.equals("medium")));
                        final int quantityNum = Math.max(0, Integer.parseInt(quantity.isEmpty() ? "1" : quantity) - (precise ? 0 : 1));

                        buf.add(new TextContent((precise ? "" : IMPRECISE_GAP_CHAR) + Strings.repeat(GAP_CHAR, quantityNum)));
                    } catch (NumberFormatException e) {
                        if (LOG.isLoggable(Level.WARNING)) {
                            LOG.log(Level.WARNING, "Invalid integer value in <gap/> attribute", e);
                        }
                    }
                } else if (xmlName.equals(handShiftName)) {
                    final String hand = data.path(newAttributeName).asText();
                    if (!hand.isEmpty()) {
                        handEnd();
                        buf.add(new TextAnnotationStart(
                                handAnnotationId = ids.next(),
                                objectMapper.createObjectNode().put(handKey, hand)
                        ));
                    }
                } else if (xmlName.equals(insertKey)) {
                    if ("right".equals(data.path(insertOrientationKey).asText())) {
                        buf.add(next);
                        tokenPending = false;
                        buf.add(new TextContent(INSERT_RIGHT_CHAR));
                    } else if ("left".equals(data.path(insertOrientationKey).asText())) {
                        insertAnnotationIds.push(annotationStart.getId());
                    }
                }

                final String stage = data.path(stageAttributeName).asText();
                if (!stage.isEmpty()) {
                    stageEnd();
                    buf.add(new TextAnnotationStart(
                            stageAnnotationId = ids.next(),
                            objectMapper.createObjectNode().put(stageKey, stage)
                    ));
                }
            } else if (next instanceof TextAnnotationEnd) {
                final String id = ((TextAnnotationEnd) next).getId();
                if (!insertAnnotationIds.isEmpty() && id.equals(insertAnnotationIds.peek())) {
                    insertAnnotationIds.pop();
                    buf.add(new TextContent(INSERT_LEFT_CHAR));
                }
            }
            if (tokenPending) {
                buf.add(next);
            }
        }

        if (buf.isEmpty()) {
            handEnd();
            stageEnd();
        }

        return !buf.isEmpty();
    }

    private void stageEnd() {
        if (stageAnnotationId != null) {
            buf.add(new TextAnnotationEnd(stageAnnotationId));
            stageAnnotationId = null;
        }
    }

    private void handEnd() {
        if (handAnnotationId != null) {
            buf.add(new TextAnnotationEnd(handAnnotationId));
            handAnnotationId = null;
        }
    }
}
