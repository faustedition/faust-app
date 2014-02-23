package eu.interedition.text.stream;

import com.google.common.base.Predicate;

import javax.annotation.Nullable;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextAnnotationEnd implements TextToken {

    public static final Predicate<TextToken> IS_INSTANCE = new Predicate<TextToken>() {
        @Override
        public boolean apply(@Nullable TextToken input) {
            return (input instanceof TextAnnotationEnd);
        }
    };

    private final String id;

    public TextAnnotationEnd(String id) {
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
