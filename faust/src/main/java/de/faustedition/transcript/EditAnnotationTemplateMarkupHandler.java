package de.faustedition.transcript;

import com.google.common.base.Predicate;
import eu.interedition.text.stream.NamespaceMapping;
import eu.interedition.text.stream.TextAnnotationStart;
import de.faustedition.textstream.TextTemplateAnnotationHandler;
import eu.interedition.text.stream.TextToken;

import javax.xml.namespace.QName;
import java.util.Set;

import static eu.interedition.text.stream.NamespaceMapping.TEI_NS_URI;
import static eu.interedition.text.stream.TextTokenPredicates.xmlName;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public class EditAnnotationTemplateMarkupHandler implements TextTemplateAnnotationHandler {

    private final Predicate<TextToken> addPredicate;
    private final Predicate<TextToken> delPredicate;
    private final Predicate<TextToken> restorePredicate;

    public EditAnnotationTemplateMarkupHandler(NamespaceMapping namespaceMapping) {
        this.addPredicate = xmlName(namespaceMapping, new QName(TEI_NS_URI, "add"));
        this.delPredicate = xmlName(namespaceMapping, new QName(TEI_NS_URI, "del"));
        this.restorePredicate = xmlName(namespaceMapping, new QName(TEI_NS_URI, "restore"));
    }

    @Override
    public boolean start(TextAnnotationStart start, Set<String> classes) {
        if (addPredicate.apply(start) || restorePredicate.apply(start)) {
            classes.add("add");
            return true;
        } else if (delPredicate.apply(start)) {
            classes.add("del");
            return true;
        }
        return false;
    }

    @Override
    public boolean end(TextAnnotationStart start, Set<String> classes) {
        if (addPredicate.apply(start) || restorePredicate.apply(start)) {
            classes.remove("add");
            return true;
        } else if (delPredicate.apply(start)) {
            classes.remove("del");
            return true;
        }
        return false;
    }
}
