package de.faustedition.index;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Predicate;
import eu.interedition.text.stream.NamespaceMapping;
import eu.interedition.text.stream.TextAnnotationStart;
import eu.interedition.text.stream.TextToken;
import eu.interedition.text.stream.TextTokenPredicates;
import de.faustedition.transcript.Hand;
import org.apache.lucene.index.Payload;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.namespace.QName;
import java.util.BitSet;

import static de.faustedition.index.TranscriptTokenAnnotation.HAND_GOETHE;
import static de.faustedition.index.TranscriptTokenAnnotation.HAND_INK;
import static de.faustedition.index.TranscriptTokenAnnotation.HAND_SCRIBE;
import static de.faustedition.index.TranscriptTokenAnnotation.SPEAKER;
import static de.faustedition.index.TranscriptTokenAnnotation.STAGE;
import static de.faustedition.index.TranscriptTokenAnnotation.VERSE;
import static eu.interedition.text.stream.NamespaceMapping.FAUST_NS_URI;
import static eu.interedition.text.stream.NamespaceMapping.TEI_NS_URI;
import static eu.interedition.text.stream.NamespaceMapping.map;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
@Singleton
public class TranscriptTokenAnnotationCodec {

    private final String handKey;
    private final Predicate<TextToken> versePredicate;
    private final Predicate<TextToken> stagePredicate;
    private final Predicate<TextToken> speakerPredicate;

    @Inject
    public TranscriptTokenAnnotationCodec(NamespaceMapping namespaceMapping) {
        this.handKey = map(namespaceMapping, new QName(FAUST_NS_URI, "hand"));
        this.versePredicate = TextTokenPredicates.xmlName(namespaceMapping, new QName(TEI_NS_URI, "l"));
        this.stagePredicate = TextTokenPredicates.xmlName(namespaceMapping, new QName(TEI_NS_URI, "stage"));
        this.speakerPredicate = TextTokenPredicates.xmlName(namespaceMapping, new QName(TEI_NS_URI, "speaker"));
    }

    public BitSet encode(TextAnnotationStart annotationStart) {
        final ObjectNode data = annotationStart.getData();
        final BitSet annotations = new BitSet();

        annotations.set(VERSE.ordinal(), versePredicate.apply(annotationStart));
        annotations.set(STAGE.ordinal(), stagePredicate.apply(annotationStart));
        annotations.set(SPEAKER.ordinal(), speakerPredicate.apply(annotationStart));

        if (data.has(handKey)) {
            final Hand hand = Hand.fromDescription(data.path(handKey).asText());
            annotations.set(HAND_GOETHE.ordinal(), "g".equals(hand.getScribalHand()));
            annotations.set(HAND_SCRIBE.ordinal(), hand.isScribe());
            annotations.set(HAND_INK.ordinal(), "t".equals(hand.getMaterial()));
        }

        return annotations;
    }

    public BitSet encode(Iterable<TranscriptTokenAnnotation> annotations) {
        final BitSet annotationSet = new BitSet();
        for (TranscriptTokenAnnotation annotation : annotations) {
            annotationSet.set(annotation.ordinal());
        }
        return annotationSet;
    }

    public static Payload asPayload(BitSet annotations) {
        return new Payload(toByteArray(annotations));
    }

    public static BitSet fromByteArray(byte[] bytes) {
        BitSet bits = new BitSet();
        for (int i = 0; i < bytes.length * 8; i++) {
            if ((bytes[bytes.length - i / 8 - 1] & (1 << (i % 8))) > 0) {
                bits.set(i);
            }
        }
        return bits;
    }

    public static byte[] toByteArray(BitSet bits) {
        byte[] bytes = new byte[bits.length() / 8 + 1];
        for (int i = 0; i < bits.length(); i++) {
            if (bits.get(i)) {
                bytes[bytes.length - i / 8 - 1] |= 1 << (i % 8);
            }
        }
        return bytes;
    }
}