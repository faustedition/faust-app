package de.faustedition.textstream;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.google.common.base.Throwables;
import com.google.common.collect.AbstractIterator;
import com.google.common.collect.Iterators;
import de.faustedition.http.LastModified;

import javax.xml.stream.XMLEventReader;
import javax.xml.stream.XMLStreamException;
import javax.xml.transform.stream.StreamSource;
import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * @author <a href="http://gregor.middell.net/" title="Homepage">Gregor Middell</a>
 */
public class FileBasedTextStream implements Iterable<TextToken>, LastModified {

    protected final List<File> sources;
    protected final long lastModified;
    protected final ObjectMapper objectMapper;
    protected final NamespaceMapping namespaceMapping;

    public FileBasedTextStream(List<File> sources, long lastModified, ObjectMapper objectMapper, NamespaceMapping namespaceMapping) {
        this.sources = sources;
        this.lastModified = lastModified;
        this.objectMapper = objectMapper;
        this.namespaceMapping = namespaceMapping;
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
            final Iterator<String> ids = XMLEvent2TextToken.ids(XMLEvent2TextToken.DEFAULT_ID_PREFIX);
            return configure(new AbstractIterator<TextToken>() {

                Iterator<File> sourceIt = sources.iterator();
                Iterator<TextToken> tokenIt = Iterators.emptyIterator();
                XMLEventReader xmlEvents = null;


                @Override
                protected TextToken computeNext() {
                    while (!tokenIt.hasNext() && sourceIt.hasNext()) {
                        try {
                            XML.closeQuietly(xmlEvents);
                            xmlEvents = XML.inputFactory().createXMLEventReader(new StreamSource(sourceIt.next()));
                            tokenIt = Iterators.transform(XML.stream(xmlEvents), new XMLEvent2TextToken(objectMapper, namespaceMapping).withIds(ids));
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
                    super.finalize();
                }
            }, ids);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    public ArrayNode json() {
        final ArrayNode text = objectMapper.createArrayNode();
        for (TextToken token : this) {
            if (token instanceof TextContent) {
                final String content = ((TextContent) token).getContent();
                if (content.length() > 0) {
                    text.add(content);
                }
            } else if (token instanceof TextAnnotationStart) {
                final TextAnnotationStart annotationStart = (TextAnnotationStart) token;
                text.addObject().put("s", annotationStart.getId()).put("d", annotationStart.getData());
            } else if (token instanceof TextAnnotationEnd) {
                text.addObject().put("e", ((TextAnnotationEnd) token).getId());
            }
        }
        return text;
    }

    protected Iterator<TextToken> configure(Iterator<TextToken> tokens, Iterator<String> ids) {
        return tokens;
    }
}
