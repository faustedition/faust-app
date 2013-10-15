package de.faustedition.xml;

import com.google.common.base.Preconditions;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import javax.xml.XMLConstants;
import java.net.URI;
import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Iterator;

public class XMLBaseTracker extends DefaultHandler {
    private static final String NULL_BASE = "";
    private Deque<String> baseStack = new ArrayDeque<String>();
    private String base;

    public XMLBaseTracker(String initialBase) {
        if (initialBase != null) {
            baseStack.push(initialBase);
            base = initialBase;
        }
    }

    public String getBase() {
        return base;
    }

    public URI getBaseURI() {
        return URI.create(base);
    }

    @Override
    public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
        String newBase = attributes.getValue(XMLConstants.XML_NS_URI, "base");
        baseStack.push(newBase == null ? NULL_BASE : newBase);
        if (newBase != null) {
            base = newBase;
        }
    }

    @Override
    public void endElement(String uri, String localName, String qName) throws SAXException {
        String lastBase = baseStack.pop();
        if (!lastBase.equals(NULL_BASE)) {
            Preconditions.checkState(lastBase.equals(base));
            base = null;

            for (Iterator<String> it = baseStack.descendingIterator(); it.hasNext(); ) {
                base = it.next();
                if (!base.equals(NULL_BASE)) {
                    break;
                }
            }

            if (base.equals(NULL_BASE)) {
                base = null;
            }
        }
    }
}
