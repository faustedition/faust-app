package de.faustedition.text;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextAnnotationStart implements TextToken {

    public static final Predicate<TextToken> IS_INSTANCE = new Predicate<TextToken>() {
        @Override
        public boolean apply(@Nullable TextToken input) {
            return (input instanceof TextAnnotationStart);
        }
    };

    private final String id;
    private final ObjectNode data;

    public TextAnnotationStart(String id, ObjectNode data) {
        this.id = id;
        this.data = data;
    }

    public String getId() {
        return id;
    }

    public ObjectNode getData() {
        return data;
    }

    @Override
    public String toString() {
        return String.format("<[%s]> %s", getId(), getData());
    }
}
