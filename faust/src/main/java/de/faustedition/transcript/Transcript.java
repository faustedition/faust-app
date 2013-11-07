package de.faustedition.transcript;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.common.base.Predicates;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterables;
import com.google.common.collect.Iterators;
import com.google.common.collect.Range;
import de.faustedition.http.LastModified;
import de.faustedition.text.LineBreaker;
import de.faustedition.text.NamespaceMapping;
import de.faustedition.text.TextContent;
import de.faustedition.text.TextToken;
import de.faustedition.text.WhitespaceCompressor;
import de.faustedition.text.XML;
import de.faustedition.text.XMLElementContextFilter;
import de.faustedition.text.XMLEvent2TextToken;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

import static de.faustedition.text.TextTokenPredicates.xmlName;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class Transcript implements Iterable<TextToken>, LastModified {

    private final List<File> sources;
    private final NamespaceMapping namespaceMapping;
    private final ObjectMapper objectMapper;
    private long lastModified;

    public Transcript(List<File> sources, NamespaceMapping namespaceMapping, ObjectMapper objectMapper, long lastModified) {
        this.sources = sources;
        this.namespaceMapping = namespaceMapping;
        this.objectMapper = objectMapper;
        this.lastModified = lastModified;
    }

    public List<File> getSources() {
        return sources;
    }

    @Override
    public Date lastModified() {
        long lastModified = this.lastModified;
        for (File source : sources) {
            lastModified = Math.max(lastModified, source.lastModified());
        }
        return new Date(lastModified);
    }

    @Override
    public Iterator<TextToken> iterator() {
        try {
            return transcriptTokens(new AbstractIterator<TextToken>() {

                Iterator<File> sourceIt = sources.iterator();
                Iterator<TextToken> tokenIt = Iterators.emptyIterator();
                XMLEventReader xmlEvents = null;

                @Override
                protected TextToken computeNext() {
                    while (!tokenIt.hasNext() && sourceIt.hasNext()) {
                        try {
                            XML.closeQuietly(xmlEvents);
                            xmlEvents = XML.inputFactory().createXMLEventReader(new StreamSource(sourceIt.next()));
                            tokenIt = Iterators.transform(XML.stream(xmlEvents), new XMLEvent2TextToken(objectMapper, namespaceMapping));
                        } catch (XMLStreamException e) {
                            XML.closeQuietly(xmlEvents);
                            throw Throwables.propagate(e);
                        }
                    }
                    if (!tokenIt.hasNext()) {
                        XML.closeQuietly(xmlEvents);
                        return endOfData();
                    }
                    return tokenIt.next();
                }

                @Override
                protected void finalize() throws Throwable {
                    XML.closeQuietly(xmlEvents);
                }
            });
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    protected Iterator<TextToken> transcriptTokens(Iterator<TextToken> tokens) {
        tokens = Iterators.filter(tokens, new XMLElementContextFilter(
                Predicates.or(
                        xmlName(namespaceMapping, "tei:teiHeader"),
                        xmlName(namespaceMapping, "tei:front"),
                        xmlName(namespaceMapping, "tei:app")
                ),
                Predicates.or(
                        xmlName(namespaceMapping, "tei:lem")
                )

        ));

        tokens = new WhitespaceCompressor(tokens, namespaceMapping, Predicates.or(
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

        tokens = new TEIMilestoneMarkupProcessor(tokens, objectMapper, namespaceMapping);

        tokens = new TranscriptMarkupHandler(tokens, objectMapper, namespaceMapping);

        tokens = new LineBreaker(tokens, Predicates.or(
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

    public String text(int start, int end) {
        final StringBuilder text = new StringBuilder();
        for (TextContent textContent : Iterables.filter(this, TextContent.class)) {
            text.append(textContent.getContent());
        }
        return text.substring(Math.max(0, start), Math.min(end, text.length()));
    }
}
