package de.faustedition.text;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TokenPredicates {

    public static Predicate<Token> xmlName(final NamespaceMapping namespaceMapping, final QName name) {
        return xmlName(namespaceMapping, NamespaceMapping.map(namespaceMapping, name));
    }

    public static Predicate<Token> xmlName(final NamespaceMapping namespaceMapping, final String name) {
        final String xmlElementNameKey = NamespaceMapping.map(namespaceMapping, XML.XML_ELEMENT_NAME);
        return Predicates.and(AnnotationStart.IS_INSTANCE, new Predicate<Token>() {
            @Override
            public boolean apply(@Nullable Token input) {
                return (name.equals(((AnnotationStart) input).getData().path(xmlElementNameKey).asText()));
            }
        });
    }

    public static Predicate<Token> hasKey(NamespaceMapping namespaceMapping, QName keyName) {
        final String key = NamespaceMapping.map(namespaceMapping, keyName);
        return Predicates.and(AnnotationStart.IS_INSTANCE, new Predicate<Token>() {
            @Override
            public boolean apply(@Nullable Token input) {
                return ((AnnotationStart) input).getData().has(key);
            }
        });
    }


}
