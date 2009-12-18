package com.bradmcevoy.http;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Stack;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;


public class PropPatchSaxHandler extends DefaultHandler {

    private Stack<String> elementPath = new Stack<String>();

    private Map<String,String> attributesCurrent; // will switch between the following
    private Map<String,String> attributesSet = new LinkedHashMap<String,String>();
    private Map<String,String> attributesRemove = new LinkedHashMap<String,String>();

    private StringBuilder sb = new StringBuilder();

    private boolean inProp;

    @Override
    public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
        if(elementPath.size() > 0 ){
            if( attributesCurrent != null ) {
                if( elementPath.peek().endsWith("prop") ) inProp = true;
            } else {
                if( elementPath.peek().endsWith("set") ) attributesCurrent = attributesSet;
                if( elementPath.peek().endsWith("remove") ) attributesCurrent = attributesRemove;
            }

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
        if( elementPath.size()>0 ) {
            if( elementPath.peek().endsWith("prop")){
                if(sb!=null) {
                    String s = sb.toString().trim();
                    attributesCurrent.put(localName,s);
                }
                sb = new StringBuilder();
            } else if( elementPath.peek().endsWith("set") ) {
                attributesCurrent = null;
            } else if( elementPath.peek().endsWith("remove") ) {
                attributesCurrent = null;
            }
        }

        super.endElement(uri, localName, name);
    }

    PropPatchHandler.Fields getFields() {
        PropPatchHandler.Fields fields = new PropPatchHandler.Fields();
        for( Map.Entry<String,String> entry : attributesSet.entrySet() ) {
            fields.setFields.add(new PropPatchHandler.SetField(entry.getKey(), entry.getValue()));
        }
        for( Map.Entry<String,String> entry : attributesRemove.entrySet() ) {
            fields.removeFields.add(new PropPatchHandler.Field(entry.getKey()));
        }
        return fields;
    }
}
