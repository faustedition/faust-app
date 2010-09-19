package de.faustedition.xml;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class MultiplexingContentHandler implements ContentHandler {

    private final ContentHandler[] handlers;

    public MultiplexingContentHandler(ContentHandler... handlers) {
        this.handlers = handlers;
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        for (ContentHandler handler : handlers) {
            handler.setDocumentLocator(locator);
        }
    }

    @Override
    public void startDocument() throws SAXException {
        for (ContentHandler handler : handlers) {
            handler.startDocument();
        }
    }

    @Override
    public void endDocument() throws SAXException {
        for (ContentHandler handler : handlers) {
            handler.endDocument();
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        for (ContentHandler handler : handlers) {
            handler.startPrefixMapping(prefix, uri);
        }
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        for (ContentHandler handler : handlers) {
            handler.endPrefixMapping(prefix);
        }
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        for (ContentHandler handler : handlers) {
            handler.startElement(uri, localName, qName, atts);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        for (ContentHandler handler : handlers) {
            handler.endElement(uri, localName, qName);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        for (ContentHandler handler : handlers) {
            handler.characters(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        for (ContentHandler handler : handlers) {
            handler.ignorableWhitespace(ch, start, length);
        }
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        for (ContentHandler handler : handlers) {
            handler.processingInstruction(target, data);
        }
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        for (ContentHandler handler : handlers) {
            handler.skippedEntity(name);
        }
    }
}
