package de.faustedition.index;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import eu.interedition.text.stream.TextAnnotationEnd;
import eu.interedition.text.stream.TextAnnotationStart;
import eu.interedition.text.stream.TextContent;
import eu.interedition.text.stream.TextToken;
import de.faustedition.transcript.Transcript;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.PayloadAttribute;
import org.apache.lucene.analysis.tokenattributes.PositionIncrementAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;

import java.io.IOException;
import java.util.BitSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Queue;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
class TranscriptTokenStream extends TokenStream {

    private final Transcript transcript;
    private final TranscriptTokenAnnotationCodec annotationCodec;

    private final TypeAttribute typeAttribute;
    private final PayloadAttribute payloadAttribute;
    private final CharTermAttribute charTermAttribute;
    private final OffsetAttribute offsetAttribute;
    private final PositionIncrementAttribute positionIncrementAttribute;

    private Iterator<TextToken> tokens;
    private Queue<TranscriptToken> buf;

    private Map<String, BitSet> annotations;
    private BitSet tokenAnnotations;

    private char lastChar;
    private int offset;
    private StringBuilder tokenContent;

    TranscriptTokenStream(Transcript transcript, TranscriptTokenAnnotationCodec annotationCodec) {
        this.transcript = transcript;
        this.annotationCodec = annotationCodec;

        this.charTermAttribute = addAttribute(CharTermAttribute.class);
        this.offsetAttribute = addAttribute(OffsetAttribute.class);
        this.positionIncrementAttribute = addAttribute(PositionIncrementAttribute.class);
        this.typeAttribute = addAttribute(TypeAttribute.class);
        this.payloadAttribute = addAttribute(PayloadAttribute.class);
    }

    @Override
    public void reset() throws IOException {
        tokens = transcript.iterator();
        buf = Lists.newLinkedList();

        annotations = Maps.newLinkedHashMap();

        lastChar = ' ';
        offset = 0;
    }

    @Override
    public boolean incrementToken() throws IOException {
        while (buf.isEmpty() && tokens.hasNext()) {
            final TextToken token = tokens.next();
            if (token instanceof TextContent) {
                for (char currentChar : ((TextContent) token).getContent().toCharArray()) {
                    if (Character.isWhitespace(currentChar)) {
                        if (!Character.isWhitespace(lastChar)) {
                            final String content = tokenContent.toString();
                            buf.add(new TranscriptToken(offset - content.length(), content, tokenAnnotations));
                            tokenContent = null;
                            tokenAnnotations = null;
                        }
                    } else {
                        if (tokenContent == null) {
                            tokenContent = new StringBuilder();
                            tokenAnnotations = new BitSet();
                            for (BitSet annotation : annotations.values()) {
                                tokenAnnotations.or(annotation);
                            }
                        }
                        tokenContent.append(currentChar);
                    }
                    lastChar = currentChar;
                    offset++;
                }
            } else if (token instanceof TextAnnotationStart) {
                final TextAnnotationStart annotationStart = (TextAnnotationStart) token;
                final BitSet annotationSet = annotationCodec.encode(annotationStart);

                annotations.put(annotationStart.getId(), annotationSet);

                if (tokenAnnotations != null) {
                    tokenAnnotations.or(annotationSet);
                }
            } else if (token instanceof TextAnnotationEnd) {
                annotations.remove(((TextAnnotationEnd) token).getId());
            }
        }

        if (buf.isEmpty() && tokenContent != null) {
            final String content = tokenContent.toString();
            buf.add(new TranscriptToken(offset - content.length(), content, tokenAnnotations));
            tokenContent = null;
            tokenAnnotations = null;
        }

        if (buf.isEmpty()) {
            return false;
        }

        final TranscriptToken token = buf.remove();
        this.positionIncrementAttribute.setPositionIncrement(1);
        this.typeAttribute.setType(TypeAttribute.DEFAULT_TYPE);
        this.charTermAttribute.setEmpty().append(token.content);
        this.offsetAttribute.setOffset(token.offset, token.offset + token.content.length());
        this.payloadAttribute.setPayload(annotationCodec.asPayload(token.annotations));

        return true;
    }

    private static class TranscriptToken {
        private final int offset;
        private final String content;
        private final BitSet annotations;

        private TranscriptToken(int offset, String content, BitSet annotations) {
            this.offset = offset;
            this.content = content;
            this.annotations = annotations;
        }
    }
}
