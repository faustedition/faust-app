package de.faustedition.transcript;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Function;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import de.faustedition.text.AnnotationEnd;
import de.faustedition.text.AnnotationStart;
import de.faustedition.text.Characters;
import de.faustedition.text.Token;

import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranscriptTokenizer implements Function<Iterator<Token>, Iterator<TranscriptToken>> {

    private Queue<TranscriptToken> buf;
    private Map<String, ObjectNode> annotations;
    private char lastChar;
    private int offset;

    private List<ObjectNode> tokenAnnotations;
    private StringBuilder tokenContent;

    @Override
    public Iterator<TranscriptToken> apply(final Iterator<Token> input) {
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
                    final Token token = input.next();
                    if (token instanceof Characters) {
                        for (char currentChar : ((Characters) token).getContent().toCharArray()) {
                            if (Character.isWhitespace(lastChar) && !Character.isWhitespace(currentChar)) {
                                emitToken();
                            }
                            tokenContent.append(lastChar = currentChar);
                            offset++;
                        }
                    } else if (token instanceof AnnotationStart) {
                        final AnnotationStart annotationStart = (AnnotationStart) token;
                        final ObjectNode annotationData = annotationStart.getData();
                        annotations.put(annotationStart.getId(), annotationData);
                        tokenAnnotations.add(annotationData);
                    } else if (token instanceof AnnotationEnd) {
                        annotations.remove(((AnnotationEnd) token).getId());
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
