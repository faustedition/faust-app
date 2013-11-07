package de.faustedition.transcript;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import de.faustedition.text.TextSegmentAnnotation;

import java.util.Arrays;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranscriptToken {

    private final String content;
    private final int offset;
    private final List<TextSegmentAnnotation> annotations;

    public TranscriptToken(String content, int offset, List<TextSegmentAnnotation> annotations) {
        this.content = content;
        this.offset = offset;
        this.annotations = annotations;
    }

    @Override
    public String toString() {
        return Joiner.on("\n").join(Arrays.asList(
                "'" + content.replaceAll("[\r\n]+", "\u00b6") + "' [" + offset + "]" ,
                Strings.repeat("-", 80),
                Joiner.on("\n").join(annotations),
                Strings.repeat("=", 80)
        ));
    }
}
