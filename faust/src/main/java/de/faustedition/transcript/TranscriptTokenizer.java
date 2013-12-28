package de.faustedition.transcript;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.base.Objects;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.collect.Range;
import de.faustedition.textstream.TextAnnotationEnd;
import de.faustedition.textstream.TextAnnotationStart;
import de.faustedition.textstream.TextContent;
import de.faustedition.textstream.TextSegmentAnnotation;
import de.faustedition.textstream.TextToken;

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
    private Map<String, Integer> annotationStarts;
    private Map<String, Integer> annotationEnds;

    private char lastChar;
    private int offset;

    private StringBuilder tokenContent;

    @Override
    public Iterator<TranscriptToken> apply(final Iterator<TextToken> input) {
        buf = Lists.newLinkedList();
        annotations = Maps.newLinkedHashMap();
        annotationStarts = Maps.newHashMap();
        annotationEnds = Maps.newHashMap();
        lastChar = ' ';
        offset = 0;

        tokenContent = new StringBuilder();

        return new AbstractIterator<TranscriptToken>() {
            @Override
            protected TranscriptToken computeNext() {
                while (buf.isEmpty() && input.hasNext()) {
                    final TextToken token = input.next();
                    if (token instanceof TextContent) {
                        for (char currentChar : ((TextContent) token).getContent().toCharArray()) {
                            if (isTokenBoundary(lastChar) && !isTokenBoundary(currentChar)) {
                                emitToken();
                            }
                            tokenContent.append(currentChar);
                            lastChar = currentChar;
                            offset++;
                        }
                    } else if (token instanceof TextAnnotationStart) {
                        final TextAnnotationStart annotationStart = (TextAnnotationStart) token;
                        final String id = annotationStart.getId();
                        annotations.put(id, annotationStart.getData());
                        annotationStarts.put(id, offset);
                    } else if (token instanceof TextAnnotationEnd) {
                        annotationEnds.put(((TextAnnotationEnd) token).getId(), offset);
                    }
                }
                if (buf.isEmpty()) {
                    emitToken();
                }
                return (buf.isEmpty() ? endOfData() : buf.remove());
            }
        };
    }

    protected boolean isTokenBoundary(char c) {
        if (Character.isWhitespace(c)) {
            return true;
        }
        final int type = Character.getType(c);
        return (Character.START_PUNCTUATION == type || Character.END_PUNCTUATION == type || Character.OTHER_PUNCTUATION == type);
    }

    protected void emitToken() {
        final String token = tokenContent.toString();
        final int length = token.length();
        if (length > 0) {
            final int tokenStart = offset - length;
            final int tokenEnd = tokenStart + token.trim().length(); // omit trailing whitespace

            final List<TextSegmentAnnotation> tokenAnnotations = Lists.newLinkedList();

            for (final Iterator<Map.Entry<String, ObjectNode>> it = annotations.entrySet().iterator(); it.hasNext(); ) {
                final Map.Entry<String, ObjectNode> annotation = it.next();
                final String annotationId = annotation.getKey();

                final int annotationStart = annotationStarts.get(annotationId);
                final int annotationEnd = Objects.firstNonNull(annotationEnds.get(annotationId), Integer.MAX_VALUE);
                if (annotationEnd > tokenStart && annotationStart < tokenEnd) {
                    tokenAnnotations.add(new TextSegmentAnnotation(
                            Range.closedOpen(Math.max(annotationStart, tokenStart), Math.min(annotationEnd, tokenEnd)),
                            annotation.getValue()
                    ));
                }

                if (annotationEnd <= tokenEnd) {
                    it.remove();
                    annotationStarts.remove(annotationId);
                    annotationEnds.remove(annotationId);
                }
            }

            buf.add(new TranscriptToken(token, tokenStart, tokenAnnotations));

            tokenContent = new StringBuilder();
        }
    }
}
