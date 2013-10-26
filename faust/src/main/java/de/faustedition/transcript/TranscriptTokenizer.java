package de.faustedition.transcript;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.faustedition.text.TextAnnotationEnd;
import de.faustedition.text.TextAnnotationStart;
import de.faustedition.text.TextContent;
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
    private char lastChar;
    private int offset;

    private List<ObjectNode> tokenAnnotations;
    private StringBuilder tokenContent;

    @Override
    public Iterator<TranscriptToken> apply(final Iterator<TextToken> input) {
        buf = Lists.newLinkedList();
        annotations = Maps.newLinkedHashMap();
        lastChar = ' ';
        offset = 0;

        tokenAnnotations = Lists.newLinkedList();
        tokenContent = new StringBuilder();

        return new AbstractIterator<TranscriptToken>() {
            @Override
            protected TranscriptToken computeNext() {
                while (buf.isEmpty() && input.hasNext()) {
                    final TextToken token = input.next();
                    if (token instanceof TextContent) {
                        for (char currentChar : ((TextContent) token).getContent().toCharArray()) {
                            if (Character.isWhitespace(lastChar) && !Character.isWhitespace(currentChar)) {
                                emitToken();
                            }
                            tokenContent.append(lastChar = currentChar);
                            offset++;
                        }
                    } else if (token instanceof TextAnnotationStart) {
                        final TextAnnotationStart annotationStart = (TextAnnotationStart) token;
                        final ObjectNode annotationData = annotationStart.getData();
                        annotations.put(annotationStart.getId(), annotationData);
                        tokenAnnotations.add(annotationData);
                    } else if (token instanceof TextAnnotationEnd) {
                        annotations.remove(((TextAnnotationEnd) token).getId());
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
        if (tokenContent.length() > 0) {
            buf.add(new TranscriptToken(tokenContent.toString(), offset, tokenAnnotations));

            tokenAnnotations = Lists.newLinkedList(annotations.values());
            tokenContent = new StringBuilder();
        }
    }
}
