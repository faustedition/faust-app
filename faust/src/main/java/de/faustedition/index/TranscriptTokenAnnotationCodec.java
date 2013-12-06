package de.faustedition.index;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Predicate;
import com.google.common.base.Splitter;
import com.google.common.collect.ImmutableSet;
import de.faustedition.text.NamespaceMapping;
import de.faustedition.text.TextAnnotationStart;
import de.faustedition.text.TextToken;
import de.faustedition.text.TextTokenPredicates;
import de.faustedition.transcript.Hand;
import org.apache.lucene.index.Payload;
import org.apache.lucene.search.DefaultSimilarity;
import org.apache.lucene.search.Similarity;

import javax.inject.Inject;
import javax.inject.Singleton;
import javax.xml.namespace.QName;
import java.nio.ByteBuffer;
import java.util.BitSet;
import java.util.List;
import java.util.Set;

import static de.faustedition.index.TranscriptTokenAnnotation.HAND_GOETHE;
import static de.faustedition.index.TranscriptTokenAnnotation.HAND_INK;
import static de.faustedition.index.TranscriptTokenAnnotation.HAND_SCRIBE;
import static de.faustedition.index.TranscriptTokenAnnotation.SPEAKER;
import static de.faustedition.index.TranscriptTokenAnnotation.STAGE;
import static de.faustedition.index.TranscriptTokenAnnotation.VERSE;
import static de.faustedition.text.NamespaceMapping.FAUST_NS_URI;
import static de.faustedition.text.NamespaceMapping.TEI_NS_URI;
import static de.faustedition.text.NamespaceMapping.map;

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

    public Similarity score(Iterable<TranscriptTokenAnnotation> annotations) {
        final BitSet annotationSet = encode(annotations);
        return new DefaultSimilarity() {
            @Override
            public float scorePayload(int docId, String fieldName, int start, int end, byte[] payload, int offset, int length) {
                final BitSet termAnnotationSet = BitSet.valueOf(ByteBuffer.wrap(payload, offset, length));
                termAnnotationSet.and(annotationSet);
                return (termAnnotationSet.equals(annotationSet) ? 1.0f : 0.0f);
            }
        };
    }

    public Payload asPayload(BitSet annotations) {
        return new Payload(annotations.toByteArray());
    }
}