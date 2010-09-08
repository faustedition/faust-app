package de.faustedition.xml;

import org.w3c.dom.Document;

public class XmlDocument {
    public static final String FAUST_NS_URI = "http://www.faustedition.net/ns";

    protected final Document dom;

    public XmlDocument() {
        this(XmlUtil.documentBuilder().newDocument());
    }

    public XmlDocument(Document document) {
        this.dom = document;
    }

    public Document getDom() {
        return dom;
    }
}
