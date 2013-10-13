package de.faustedition.text;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TokenPredicates {

    public static Predicate<Token> name(final NamespaceMapping namespaceMapping, final QName name) {
        return name(namespaceMapping, NamespaceMapping.map(namespaceMapping, name));
    }

    public static Predicate<Token> name(final NamespaceMapping namespaceMapping, final String name) {
        final String xmlElementNameKey = NamespaceMapping.map(namespaceMapping, XML.XML_ELEMENT_NAME);
        return Predicates.and(AnnotationStart.IS_INSTANCE, new Predicate<Token>() {
            @Override
            public boolean apply(@Nullable Token input) {
                return (name.equals(((AnnotationStart) input).getData().path(xmlElementNameKey).asText()));
            }
        });
    }
}
