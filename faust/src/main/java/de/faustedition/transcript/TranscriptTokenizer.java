package de.faustedition.transcript;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.base.Functions;
import com.google.common.base.Objects;
import com.google.common.base.Predicate;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import de.faustedition.text.TextAnnotationEnd;
import de.faustedition.text.TextAnnotationStart;
import de.faustedition.text.TextContent;
import de.faustedition.text.TextSegmentAnnotation;
import de.faustedition.text.TextToken;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranscriptTokenizer implements Function<Iterator<TextToken>, Iterator<TranscriptToken>> {

    private Queue<TranscriptToken> buf;
    private Map<String, ObjectNode> annotations;
    private Map<String,  Integer> annotationStarts;
    private Map<String, Integer> annotationEnds;

    private char lastChar;
    private int offset;

    private Map<String, ObjectNode> tokenAnnotations;
    private StringBuilder tokenContent;

    @Override
    public Iterator<TranscriptToken> apply(final Iterator<TextToken> input) {
        buf = Lists.newLinkedList();
        annotations = Maps.newLinkedHashMap();
        annotationStarts = Maps.newHashMap();
        annotationEnds = Maps.newHashMap();
        lastChar = ' ';
        offset = 0;

        tokenAnnotations = Maps.newLinkedHashMap();
        tokenContent = new StringBuilder();

        return new AbstractIterator<TranscriptToken>() {
            @Override
            protected TranscriptToken computeNext() {
                while (buf.isEmpty() && input.hasNext()) {
                    final TextToken token = input.next();
                    if (token instanceof TextContent) {
                        for (char currentChar : ((TextContent) token).getContent().toCharArray()) {
                            final boolean currentIsWhitespace = Character.isWhitespace(currentChar);
                            if (!Character.isWhitespace(lastChar) && currentIsWhitespace) {
                                emitToken();
                            }
                            if (!currentIsWhitespace) {
                                tokenContent.append(currentChar);
                            }
                            lastChar = currentChar;
                            offset++;
                        }
                    } else if (token instanceof TextAnnotationStart) {
                        final TextAnnotationStart annotationStart = (TextAnnotationStart) token;
                        final String id = annotationStart.getId();
                        final ObjectNode data = annotationStart.getData();

                        annotations.put(id, data);
                        annotationStarts.put(id, offset);
                        tokenAnnotations.put(id, data);
                    } else if (token instanceof TextAnnotationEnd) {
                        final String id = ((TextAnnotationEnd) token).getId();
                        annotations.remove(id);
                        annotationEnds.put(id, offset);
                    }
                }
                if (buf.isEmpty()) {
                    emitToken();
                }
                return (buf.isEmpty() ? endOfData() : buf.remove());
            }
        };
    }

    protected void emitToken() {
        final int length = tokenContent.length();
        if (length > 0) {
            final int tokenStart = offset - length;
            final int tokenEnd = offset;

            final List<TextSegmentAnnotation> annotations = Lists.newArrayListWithExpectedSize(tokenAnnotations.size());
            for (Map.Entry<String, ObjectNode> annotation : tokenAnnotations.entrySet()) {
                final String annotationId = annotation.getKey();
                final int annotationStart = Math.max(annotationStarts.get(annotationId), tokenStart);
                final int annotationEnd = Objects.firstNonNull(annotationEnds.get(annotationId), tokenEnd);
                if (annotationEnd > tokenStart && annotationStart < tokenEnd) {
                    annotations.add(new TextSegmentAnnotation(
                            Range.closedOpen(annotationStart, annotationEnd),
                            annotation.getValue()
                    ));
                }
            }
            buf.add(new TranscriptToken(tokenContent.toString(), tokenStart, annotations));

            tokenAnnotations = Maps.newLinkedHashMap(this.annotations);
            tokenContent = new StringBuilder();

            for (Iterator<Map.Entry<String, Integer>> it = annotationEnds.entrySet().iterator(); it.hasNext() ; ) {
                final Map.Entry<String, Integer> annotationEnd = it.next();
                if (annotationEnd.getValue() <= offset) {
                    annotationStarts.remove(annotationEnd.getKey());
                    it.remove();
                }
            }
        }
    }
}
