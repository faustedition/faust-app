package com.bradmcevoy.http;

import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.bradmcevoy.io.FileUtils;

public class XmlWriter {
    private Logger log = LoggerFactory.getLogger(XmlWriter.class);
    public enum Type {
        OPENING,
        CLOSING,
        NO_CONTENT        
    };
    
    protected final Writer writer;
            
    public XmlWriter(OutputStream out) {        
        this.writer = new PrintWriter(out,true);
    }
        
    private void append(String value) {
        try {
            writer.write(value);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    private void append(char c) {
        try {
            writer.write((int)c);
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void writeProperty(String namespace, String namespaceInfo,String name, String value) {
        writeElement(namespace, namespaceInfo, name, Type.OPENING);
        append(value);
        writeElement(namespace, namespaceInfo, name, Type.CLOSING);

    }

    public void writeProperty(String namespace, String name, String value) {        
        if( value == null ) {
            writeProperty( namespace, name );
        } else {
            writeElement(namespace, name, Type.OPENING);
            append(value);
            writeElement(namespace, name, Type.CLOSING);
        }
    }


    public void writeProperty(String namespace, String name) {
        writeElement(namespace, name, Type.NO_CONTENT);
    }


    public void writeElement(String namespace, String name, Type type) {
        writeElement(namespace, null, name, type);
    }

    public void open(String namespace,String name) {
        writeElement(namespace,name,Type.OPENING);
    }

    public void close(String namespace,String name) {
        writeElement(namespace,name,Type.CLOSING);
    }

    public void open(String name) {
        writeElement(null,name,Type.OPENING);
    }

    public void close(String name) {
        writeElement(null,name,Type.CLOSING);
    }
      
    
    
    
    public class Element {
        private final String name;
        
        Element(String name) {
            this.name = name;
            append("<");
            append(name);
        }
        
        public Element writeAtt(String name, String value) {
            append(" ");
            append(name);
            append("=");
            append((char)34);
            append(value);
            append((char)34);
            return this;
        }
     
        public Element writeText(String text) {
            append(text);
            return this;
        }        
        
        public Element open() {
            append(">\n");
            return this;
        }

        public Element close() {
            append("</" + name + ">\n");
            return this;
        }
        
        public Element noContent() {
            append("/>\n");
            return this;
        }

    }
    
    
    
    
    public Element begin(String name) {
        Element el = new Element(name);
        return el;
    }
    
    public void writeElement(String namespace, String namespaceInfo,String name, Type type) {
        if ((namespace != null) && (namespace.length() > 0)) {
            switch (type) {
            case OPENING:
                if (namespaceInfo != null) {
                    append("<" + namespace + ":" + name + " xmlns:"+ namespace + "=\""+ namespaceInfo + "\">");
                } else {
                    append("<" + namespace + ":" + name + ">");
                }
                break;
            case CLOSING:
                append("</" + namespace + ":" + name + ">\n");
                break;
            case NO_CONTENT:
            default:
                if (namespaceInfo != null) {
                    append("<" + namespace + ":" + name + " xmlns:"+ namespace + "=\"" + namespaceInfo + "\"/>");
                } else {
                    append("<" + namespace + ":" + name + "/>");
                }
                break;
            }
        } else {
            switch (type) {
            case OPENING:
                append("<" + name + ">");
                break;
            case CLOSING:
                append("</" + name + ">\n");
                break;
            case NO_CONTENT:
            default:
                append("<" + name + "/>");
                break;
            }
        }
    }


    /**
     * Append plain text.
     *
     * @param text Text to append
     */
    public void writeText(String text) {
        append(text);
    }


    /**
     * Write a CDATA segment.
     *
     * @param data Data to append
     */
    public void writeData(String data) {
        append("<![CDATA[" + data + "]]>");
    }


    public void writeXMLHeader() {
        append("<?xml version=\"1.0\" encoding=\"utf-8\" ?>\n");
    }


    /**
     * Send data and reinitializes buffer.
     */
    public void flush()  {
        try {
            writer.flush();
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }
    
    public void sample(InputStream in) {
        log.debug("outputting sample");
        try {
            ByteArrayOutputStream out = FileUtils.readIn(in);
            writer.write(out.toString());
        } catch (FileNotFoundException ex) {
            log.error("",ex);
        } catch (IOException ex) {
            log.error("",ex);
        } finally {
            FileUtils.close(in);
        }
    }

    public void newLine() {
        append("\n");
    }
}
