package com.bradmcevoy.http;

import java.util.HashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class PropFindSaxHandler extends DefaultHandler {

    private Stack<String> elementPath = new Stack<String>();
    
    private Map<String,String> attributes = new HashMap<String,String>();

    private StringBuilder sb = new StringBuilder();

    private boolean inProp;
    
    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if(elementPath.size() > 0 && elementPath.peek().endsWith("prop")){
            inProp = true;
        }
        elementPath.push(localName);
        super.startElement(uri, localName, name, attributes);
    }

    @Override
    public void characters(char[] ch, int start, int length) throws SAXException {
        if(inProp){
            sb.append(ch,start,length);
        }
    }

    @Override
    public void endElement(String uri, String localName, String name) throws SAXException {
        elementPath.pop();
        if(elementPath.size()>0 && elementPath.peek().endsWith("prop")){
            if(sb!=null)
                getAttributes().put(localName,sb.toString().trim());
            sb.delete(0, sb.length());
        }
            
        super.endElement(uri, localName, name);
    }

    public Map<String,String> getAttributes() {
        return attributes;
    }


}
