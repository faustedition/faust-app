package de.faustedition.transcript;

import com.google.common.base.Joiner;
import com.google.common.base.Strings;
import com.google.common.collect.Range;
import de.faustedition.textstream.TextSegmentAnnotation;

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

    public String getContent() {
        return content;
    }

    public int getOffset() {
        return offset;
    }

    public Range<Integer> getSegment() {
        return Range.closedOpen(offset, offset + content.length());
    }

    public List<TextSegmentAnnotation> getAnnotations() {
        return annotations;
    }

    @Override
    public String toString() {
        return Joiner.on("\n").join(
                "'" + content.replaceAll("[\r\n]+", "\u00b6") + "' [" + offset + "]" ,
                Strings.repeat("-", 80),
                Joiner.on("\n").join(annotations),
                Strings.repeat("=", 80)
        );
    }
}
