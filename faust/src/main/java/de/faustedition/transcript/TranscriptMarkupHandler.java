package de.faustedition.transcript;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.collect.ForwardingIterator;
import com.google.common.collect.Lists;
import de.faustedition.text.Annotation;
import de.faustedition.text.AnnotationEnd;
import de.faustedition.text.AnnotationStart;
import de.faustedition.text.NamespaceMapping;
import de.faustedition.text.Token;
import de.faustedition.text.XML;

import javax.xml.namespace.QName;
import java.util.Iterator;
import java.util.Queue;

import static de.faustedition.text.NamespaceMapping.FAUST_NS_URI;
import static de.faustedition.text.NamespaceMapping.TEI_NS_URI;
import static de.faustedition.text.NamespaceMapping.TEI_SIG_GE_URI;
import static de.faustedition.text.NamespaceMapping.map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranscriptMarkupHandler extends ForwardingIterator<Token> {

    private final Iterator<Token> delegate;
    private final ObjectMapper objectMapper;
    private final Queue<Token> buf = Lists.newLinkedList();
    private final Iterator<String> ids = Annotation.ids("transcript_");

    private final String stageAttributeName;
    private final String stageKey;

    private final String handShiftName;
    private final String handKey;
    private final String newAttributeName;
    private final String xmlNameKey;
    private final String documentKey;

    private String stageAnnotationId = null;
    private String handAnnotationId = null;

    public TranscriptMarkupHandler(Iterator<Token> delegate, ObjectMapper objectMapper, NamespaceMapping namespaceMapping) {
        this.delegate = delegate;
        this.objectMapper = objectMapper;

        this.xmlNameKey = map(namespaceMapping, XML.XML_ELEMENT_NAME);

        this.stageKey = map(namespaceMapping, new QName(FAUST_NS_URI, "stage"));
        this.stageAttributeName = map(namespaceMapping, new QName(TEI_SIG_GE_URI, "stage"));

        this.handKey = map(namespaceMapping, new QName(FAUST_NS_URI, "hand"));
        this.handShiftName = map(namespaceMapping, new QName(TEI_NS_URI, "handShift"));
        this.newAttributeName = map(namespaceMapping, new QName(TEI_NS_URI, "new"));

        this.documentKey = map(namespaceMapping, new QName(TEI_SIG_GE_URI, "document"));
    }

    @Override
    protected Iterator<Token> delegate() {
        return delegate;
    }

    @Override
    public Token next() {
        return buf.remove();
    }

    @Override
    public boolean hasNext() {
        if (buf.isEmpty() && super.hasNext()) {
            final Token next = super.next();
            if (next instanceof AnnotationStart) {
                final AnnotationStart annotationStart = (AnnotationStart) next;
                final ObjectNode data = annotationStart.getData();

                final String xmlName = data.path(xmlNameKey).asText();
                if (xmlName.equals(documentKey)) {
                    handEnd();
                    stageEnd();
                } else if (xmlName.equals(handShiftName)) {
                    final String hand = data.path(newAttributeName).asText();
                    if (!hand.isEmpty()) {
                        handEnd();
                        buf.add(new AnnotationStart(
                                handAnnotationId = ids.next(),
                                objectMapper.createObjectNode().put(handKey, hand)
                        ));
                    }
                }

                final String stage = data.path(stageAttributeName).asText();
                if (!stage.isEmpty()) {
                    stageEnd();
                    buf.add(new AnnotationStart(
                            stageAnnotationId = ids.next(),
                            objectMapper.createObjectNode().put(stageKey, stage)
                    ));
                }


            }
            buf.add(next);
        }

        if (buf.isEmpty()) {
            handEnd();
            stageEnd();
        }

        return !buf.isEmpty();
    }

    private void stageEnd() {
        if (stageAnnotationId != null) {
            buf.add(new AnnotationEnd(stageAnnotationId));
            stageAnnotationId = null;
        }
    }

    private void handEnd() {
        if (handAnnotationId != null) {
            buf.add(new AnnotationEnd(handAnnotationId));
            handAnnotationId = null;
        }
    }
}
