package eu.interedition.text.stream;

import com.google.common.base.Predicate;
import com.google.common.base.Predicates;

import javax.annotation.Nullable;
import javax.xml.namespace.QName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class TextTokenPredicates {

    public static Predicate<TextToken> xmlName(final NamespaceMapping namespaceMapping, final QName name) {
        return xmlName(namespaceMapping, NamespaceMapping.map(namespaceMapping, name));
    }

    public static Predicate<TextToken> xmlName(final NamespaceMapping namespaceMapping, final String name) {
        final String xmlElementNameKey = NamespaceMapping.map(namespaceMapping, XML.XML_ELEMENT_NAME);
        return Predicates.and(TextAnnotationStart.IS_INSTANCE, new Predicate<TextToken>() {
            @Override
            public boolean apply(@Nullable TextToken input) {
                return (name.equals(((TextAnnotationStart) input).getData().path(xmlElementNameKey).asText()));
            }
        });
    }

    public static Predicate<TextToken> hasKey(NamespaceMapping namespaceMapping, QName keyName) {
        final String key = NamespaceMapping.map(namespaceMapping, keyName);
        return Predicates.and(TextAnnotationStart.IS_INSTANCE, new Predicate<TextToken>() {
            @Override
            public boolean apply(@Nullable TextToken input) {
                return ((TextAnnotationStart) input).getData().has(key);
            }
        });
    }


}
