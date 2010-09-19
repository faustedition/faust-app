package de.faustedition.xml;

import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.ContentHandler;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;

public class XMLFragmentFilter implements ContentHandler {

    private final ContentHandler filtered;
    private final String nsUri;
    private final String localName;

    private boolean inFragment;
    private Stack<String> qNameStack;

    public XMLFragmentFilter(ContentHandler filtered, String nsUri, String localName) {
        this.filtered = filtered;
        this.nsUri = nsUri;
        this.localName = localName;
    }

    @Override
    public void startDocument() throws SAXException {
        inFragment = false;
        qNameStack = new Stack<String>();
        filtered.startDocument();
    }

    @Override
    public void endDocument() throws SAXException {
        filtered.endDocument();
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (qNameStack.isEmpty() && this.localName.equals(localName) && nsUri.equals(uri)) {
            inFragment = true;
        }
        if (inFragment) {
            qNameStack.push(qName);
            filtered.startElement(uri, localName, qName, atts);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (inFragment) {
            qNameStack.pop();
            if (this.localName.equals(localName) && nsUri.equals(uri) && qNameStack.isEmpty()) {
                inFragment = false;
            }
            filtered.endElement(uri, localName, qName);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inFragment) {
            filtered.characters(ch, start, length);
        }
    }

    @Override
    public void ignorableWhitespace(char[] ch, int start, int length) throws SAXException {
        if (inFragment) {
            filtered.ignorableWhitespace(ch, start, length);
        }
    }

    @Override
    public void startPrefixMapping(String prefix, String uri) throws SAXException {
        filtered.startPrefixMapping(prefix, uri);
    }

    @Override
    public void endPrefixMapping(String prefix) throws SAXException {
        filtered.endPrefixMapping(prefix);
    }

    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        if (inFragment) {
            filtered.processingInstruction(target, data);
        }
    }

    @Override
    public void skippedEntity(String name) throws SAXException {
        if (inFragment) {
            filtered.skippedEntity(name);
        }
    }

    @Override
    public void setDocumentLocator(Locator locator) {
        filtered.setDocumentLocator(locator);
    }
}
