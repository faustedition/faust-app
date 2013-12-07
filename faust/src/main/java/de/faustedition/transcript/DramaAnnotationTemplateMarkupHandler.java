package de.faustedition.transcript;

import com.google.common.base.Predicate;
import de.faustedition.text.NamespaceMapping;
import de.faustedition.text.TextAnnotationStart;
import de.faustedition.text.TextTemplateAnnotationHandler;
import de.faustedition.text.TextToken;

import javax.xml.namespace.QName;
import java.util.Set;

import static de.faustedition.text.NamespaceMapping.TEI_NS_URI;
import static de.faustedition.text.TextTokenPredicates.xmlName;

/**
* @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
*/
public class DramaAnnotationTemplateMarkupHandler implements TextTemplateAnnotationHandler {

    private final Predicate<TextToken> stagePredicate;
    private final Predicate<TextToken> speakerPredicate;

    public DramaAnnotationTemplateMarkupHandler(NamespaceMapping namespaceMapping) {
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
        }
        return false;
    }
}
