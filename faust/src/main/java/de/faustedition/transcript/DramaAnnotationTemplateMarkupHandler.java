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
public class DramaAnnotationTemplateMarkupHandler implements TextTemplateAnnotationHandler {

    private final Predicate<TextToken> headPredicate;
    private final Predicate<TextToken> stagePredicate;
    private final Predicate<TextToken> speakerPredicate;

    public DramaAnnotationTemplateMarkupHandler(NamespaceMapping namespaceMapping) {
        this.headPredicate = xmlName(namespaceMapping, new QName(TEI_NS_URI, "head"));
        this.stagePredicate = xmlName(namespaceMapping, new QName(TEI_NS_URI, "stage"));
        this.speakerPredicate = xmlName(namespaceMapping, new QName(TEI_NS_URI, "speaker"));
    }

    @Override
    public boolean start(TextAnnotationStart start, Set<String> classes) {
        if (stagePredicate.apply(start)) {
            classes.add("stage");
            return true;
        } else if (speakerPredicate.apply(start)) {
            classes.add("speaker");
            return true;
        } else if (headPredicate.apply(start)) {
            classes.add("head");
            return true;
        }
        return false;
    }

    @Override
    public boolean end(TextAnnotationStart start, Set<String> classes) {
        if (stagePredicate.apply(start)) {
            classes.remove("stage");
            return true;
        } else if (speakerPredicate.apply(start)) {
            classes.remove("speaker");
            return true;
        }  else if (headPredicate.apply(start)) {
            classes.remove("head");
            return true;
        }
        return false;
    }
}
