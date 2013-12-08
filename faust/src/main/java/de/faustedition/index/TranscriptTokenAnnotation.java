package de.faustedition.index;

import java.util.Arrays;
import java.util.BitSet;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public enum TranscriptTokenAnnotation {
    STAGE,
    SPEAKER,
    VERSE,
    HAND_GOETHE,
    HAND_SCRIBE,
    HAND_INK;

    public static BitSet encode(TranscriptTokenAnnotation... annotations) {
        return encode(Arrays.asList(annotations));
    }

    public static BitSet encode(Iterable<TranscriptTokenAnnotation> annotations) {
        final BitSet annotationSet = new BitSet();
        for (TranscriptTokenAnnotation annotation : annotations) {
            annotationSet.set(annotation.ordinal());
        }
        return annotationSet;
    }
}
