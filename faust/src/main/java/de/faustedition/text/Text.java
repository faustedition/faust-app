package de.faustedition.text;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterators;
import de.faustedition.textstream.FileBasedTextStream;
import de.faustedition.textstream.LineBreaker;
import de.faustedition.textstream.NamespaceMapping;
import de.faustedition.textstream.TextToken;
import de.faustedition.textstream.WhitespaceCompressor;
import de.faustedition.textstream.XMLElementContextFilter;
import de.faustedition.transcript.TEIMilestoneMarkupProcessor;
import de.faustedition.transcript.TranscriptMarkupHandler;

import java.io.File;
import java.util.Iterator;
import java.util.List;

import static de.faustedition.textstream.TextTokenPredicates.xmlName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Text extends FileBasedTextStream {

    public Text(List<File> sources, long lastModified, ObjectMapper objectMapper, NamespaceMapping namespaceMapping) {
        super(sources, lastModified, objectMapper, namespaceMapping);
    }

    protected Iterator<TextToken> configure(Iterator<TextToken> tokens, Iterator<String> ids) {
        tokens = Iterators.filter(tokens, new XMLElementContextFilter(
                Predicates.<TextToken>or(
                        xmlName(namespaceMapping, "tei:teiHeader"),
                        xmlName(namespaceMapping, "tei:front"),
                        xmlName(namespaceMapping, "tei:app")
                ),
                Predicates.<TextToken>or(
                        xmlName(namespaceMapping, "tei:lem")
                )

        ));

        tokens = new WhitespaceCompressor(tokens, namespaceMapping, Predicates.<TextToken>or(
                xmlName(namespaceMapping, "tei:TEI"),
                xmlName(namespaceMapping, "tei:body"),
                xmlName(namespaceMapping, "tei:group"),
                xmlName(namespaceMapping, "tei:text"),
                xmlName(namespaceMapping, "tei:div"),
                xmlName(namespaceMapping, "tei:lg"),
                xmlName(namespaceMapping, "tei:sp"),
                xmlName(namespaceMapping, "tei:subst"),
                xmlName(namespaceMapping, "tei:choice")
        ));

        tokens = new TEIMilestoneMarkupProcessor(tokens, objectMapper, namespaceMapping).withIds(ids);

        tokens = new LineBreaker(tokens, Predicates.<TextToken>or(
                xmlName(namespaceMapping, "tei:text"),
                xmlName(namespaceMapping, "tei:div"),
                xmlName(namespaceMapping, "tei:head"),
                xmlName(namespaceMapping, "tei:sp"),
                xmlName(namespaceMapping, "tei:stage"),
                xmlName(namespaceMapping, "tei:speaker"),
                xmlName(namespaceMapping, "tei:lg"),
                xmlName(namespaceMapping, "tei:l"),
                xmlName(namespaceMapping, "tei:p"),
                xmlName(namespaceMapping, "tei:ab"),
                xmlName(namespaceMapping, "tei:line"),
                xmlName(namespaceMapping, "ge:line")
        ));

        return tokens;
    }
}
