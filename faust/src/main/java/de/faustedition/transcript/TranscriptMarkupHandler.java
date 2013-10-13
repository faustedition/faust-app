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
    private final Iterator<String> ids = Annotation.ids("transcript_annotation_");

    private final String facsimileKey;
    private final String graphicName;
    private final String mimeTypeAttributeName;
    private final String urlAttributeName;

    private final String stageAttributeName;
    private final String stageKey;

    private final String handShiftName;
    private final String handKey;
    private final String newAttributeName;
    private final String xmlNameKey;

    private String facsimileAnnotationId = null;
    private String currentStageAnnotationId = null;
    private String currentHandAnnotationId = null;

    public TranscriptMarkupHandler(Iterator<Token> delegate, ObjectMapper objectMapper, NamespaceMapping namespaceMapping) {
        this.delegate = delegate;
        this.objectMapper = objectMapper;

        this.xmlNameKey = map(namespaceMapping, XML.XML_ELEMENT_NAME);

        this.stageKey = map(namespaceMapping, new QName(FAUST_NS_URI, "stage"));
        this.stageAttributeName = map(namespaceMapping, new QName(TEI_SIG_GE_URI, "stage"));

        this.handKey = map(namespaceMapping, new QName(FAUST_NS_URI, "hand"));
        this.handShiftName = map(namespaceMapping, new QName(TEI_NS_URI, "handShift"));
        this.newAttributeName = map(namespaceMapping, new QName(TEI_NS_URI, "new"));

        this.facsimileKey = map(namespaceMapping, new QName(FAUST_NS_URI, "facsimile"));
        this.graphicName = map(namespaceMapping, new QName(TEI_NS_URI, "graphic"));
        this.mimeTypeAttributeName = map(namespaceMapping, new QName(TEI_NS_URI, "mimeType"));
        this.urlAttributeName = map(namespaceMapping, new QName(TEI_NS_URI, "url"));
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
                final ObjectNode data = ((AnnotationStart) next).getData();
                final String xmlName = data.path(xmlNameKey).asText();

                // generate facsimile annotation only once per transcript and filter text-image-link elements (@mimeType)
                if (facsimileAnnotationId == null && xmlName.equals(graphicName) && !data.has(mimeTypeAttributeName)) {
                    final String url = data.path(urlAttributeName).asText();
                    if (!url.isEmpty()) {
                        buf.add(new AnnotationStart(
                                facsimileAnnotationId = ids.next(),
                                objectMapper.createObjectNode().put(facsimileKey, url)
                        ));
                    }
                }

                final String stage = data.path(stageAttributeName).asText();
                if (!stage.isEmpty()) {
                    stageEnd();
                    buf.add(new AnnotationStart(
                            currentStageAnnotationId = ids.next(),
                            objectMapper.createObjectNode().put(stageKey, stage)
                    ));
                }

                if (xmlName.equals(handShiftName)) {
                    final String hand = data.path(newAttributeName).asText();
                    if (!hand.isEmpty()) {
                        handEnd();
                        buf.add(new AnnotationStart(
                                currentHandAnnotationId = ids.next(),
                                objectMapper.createObjectNode().put(handKey, hand)
                        ));
                    }
                }
            }
            buf.add(next);
        }

        if (buf.isEmpty()) {
            handEnd();
            stageEnd();
            facsimileEnd();
            return false;
        }

        return true;
    }

    private void facsimileEnd() {
        if (facsimileAnnotationId != null) {
            buf.add(new AnnotationEnd(facsimileAnnotationId));
            facsimileAnnotationId = null;
        }
    }
    private void stageEnd() {
        if (currentStageAnnotationId != null) {
            buf.add(new AnnotationEnd(currentStageAnnotationId));
            currentStageAnnotationId = null;
        }
    }

    private void handEnd() {
        if (currentHandAnnotationId != null) {
            buf.add(new AnnotationEnd(currentHandAnnotationId));
            currentHandAnnotationId = null;
        }
    }
}
