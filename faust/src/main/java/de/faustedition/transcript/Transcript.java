package de.faustedition.transcript;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicate;
import com.google.common.base.Predicates;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import de.faustedition.textstream.FileBasedTextStream;
import eu.interedition.text.stream.LineBreaker;
import eu.interedition.text.stream.NamespaceMapping;
import eu.interedition.text.stream.SegmentRangeFilter;
import eu.interedition.text.stream.TextAnnotationStart;
import eu.interedition.text.stream.TextContent;
import eu.interedition.text.stream.TextRangeFilter;
import eu.interedition.text.stream.TextToken;
import eu.interedition.text.stream.WhitespaceCompressor;
import eu.interedition.text.stream.XMLElementContextFilter;

import javax.xml.namespace.QName;
import java.io.File;
import java.util.Iterator;
import java.util.List;

import static eu.interedition.text.stream.NamespaceMapping.TEI_NS_URI;
import static eu.interedition.text.stream.NamespaceMapping.TEI_SIG_GE_URI;
import static eu.interedition.text.stream.TextTokenPredicates.xmlName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Transcript extends FileBasedTextStream {

    private Predicate<TextAnnotationStart> segmentStart;

    public Transcript(List<File> sources, long lastModified, ObjectMapper objectMapper, NamespaceMapping namespaceMapping) {
        super(sources, lastModified, objectMapper, namespaceMapping);

        this.segmentStart = Predicates.or(
                xmlName(namespaceMapping, new QName(TEI_NS_URI, "l")),
                xmlName(namespaceMapping, new QName(TEI_NS_URI, "line")),
                xmlName(namespaceMapping, new QName(TEI_NS_URI, "p")),
                xmlName(namespaceMapping, new QName(TEI_NS_URI, "ab")),
                xmlName(namespaceMapping, new QName(TEI_SIG_GE_URI, "line"))
        );
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
                xmlName(namespaceMapping, "tei:choice"),
                xmlName(namespaceMapping, "tei:surface"),
                xmlName(namespaceMapping, "tei:zone"),
                xmlName(namespaceMapping, "ge:document"),
                xmlName(namespaceMapping, "f:overw")
        ));

        tokens = new TEIMilestoneMarkupProcessor(tokens, objectMapper, namespaceMapping).withIds(ids);

        tokens = new TranscriptMarkupHandler(tokens, objectMapper, namespaceMapping).withIds(ids);

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

    public Iterable<TextToken> range(final int start, final int end) {
        return new Iterable<TextToken>() {
            @Override
            public Iterator<TextToken> iterator() {
                return new TextRangeFilter(Transcript.this.iterator(), start, end);
            }
        };
    }

    public Iterable<TextToken> segment(final int start, final int end) {
        return new Iterable<TextToken>() {
            @Override
            public Iterator<TextToken> iterator() {
                return new SegmentRangeFilter(Transcript.this.iterator(), segmentStart, start, end);
            }
        };
    }

    public String rangeText(int start, int end) {
        final StringBuilder text = new StringBuilder();
        for (TextContent textContent : Iterables.filter(range(start, end), TextContent.class)) {
            text.append(textContent.getContent());
        }
        return text.toString();
    }

    public String segmentText(int start, int end) {
        final StringBuilder text = new StringBuilder();
        for (TextContent textContent : Iterables.filter(segment(start, end), TextContent.class)) {
            text.append(textContent.getContent());
        }
        return text.toString();
    }
}
