package de.faustedition.transcript;

import com.fasterxml.jackson.databind.node.ObjectNode;

import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TranscriptToken {

    private final String content;
    private final int offset;
    private final List<ObjectNode> annotations;

    public TranscriptToken(String content, int offset, List<ObjectNode> annotations) {
        this.content = content;
        this.offset = offset;
        this.annotations = annotations;
    }
}
