package de.faustedition.text;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Characters implements Token {

    public static final Predicate<Object> IS_INSTANCE = Predicates.instanceOf(Characters.class);

    public static final Predicate<Token> EMPTY_CONTENT = Predicates.and(IS_INSTANCE, new Predicate<Token>() {
        @Override
        public boolean apply(@Nullable Token input) {
            return (((Characters) input).getContent().length() == 0);
        }
    });

    private final String content;

    public Characters(String content) {
        this.content = content;
    }

    public String getContent() {
        return content;
    }

    @Override
    public String toString() {
        return ("'" + getContent().replaceAll("[\r\n]+", "\u00b6") + "'");
    }
}
