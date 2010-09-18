package de.faustedition.xml;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLFilterImpl;

public class XMLFragmentFilter extends XMLFilterImpl {

    private final String nsUri;
    private final String localName;
    private boolean inFragment = false;

    public XMLFragmentFilter(XMLReader parent, String nsUri, String localName) {
        super(parent);
        this.nsUri = nsUri;
        this.localName = localName;
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes atts) throws SAXException {
        if (this.localName.equals(localName) && nsUri.equals(uri)) {
            inFragment = true;
        }
        if (inFragment) {
            super.startElement(uri, localName, qName, atts);
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        if (inFragment) {
            if (this.localName.equals(localName) && nsUri.equals(uri)) {
                inFragment = false;
            }
            super.endElement(uri, localName, qName);
        }
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if (inFragment) {
            super.characters(ch, start, length);
        }
    }
    
    @Override
    public void processingInstruction(String target, String data) throws SAXException {
        if (inFragment) {
            super.processingInstruction(target, data);
        }
    }
}
