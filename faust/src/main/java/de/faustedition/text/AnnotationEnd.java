package de.faustedition.text;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class AnnotationEnd implements Token {

    public static final Predicate<Token> IS_INSTANCE = new Predicate<Token>() {
        @Override
        public boolean apply(@Nullable Token input) {
            return (input instanceof AnnotationEnd);
        }
    };

    private final String id;

    public AnnotationEnd(String id) {
        this.id = id;
    }

    public String getId() {
        return id;
    }

    @Override
    public String toString() {
        return String.format("</[%s]>", getId());
    }
}
