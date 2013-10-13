package de.faustedition.text;

import com.fasterxml.jackson.databind.node.ObjectNode;
import com.google.common.base.Predicate;

import javax.annotation.Nullable;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationStart implements Token {

    public static final Predicate<Token> IS_INSTANCE = new Predicate<Token>() {
        @Override
        public boolean apply(@Nullable Token input) {
            return (input instanceof AnnotationStart);
        }
    };

    private final String id;
    private final ObjectNode data;

    public AnnotationStart(String id, ObjectNode data) {
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
