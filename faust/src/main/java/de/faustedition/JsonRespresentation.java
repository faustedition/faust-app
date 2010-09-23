package de.faustedition;

import java.io.IOException;
import java.io.OutputStream;

import org.codehaus.jackson.JsonEncoding;
import org.codehaus.jackson.JsonFactory;
import org.codehaus.jackson.JsonGenerator;
import org.restlet.data.CharacterSet;
import org.restlet.data.MediaType;
import org.restlet.representation.OutputRepresentation;

public abstract class JsonRespresentation extends OutputRepresentation {

    protected JsonGenerator generator;

    public JsonRespresentation() {
        super(MediaType.APPLICATION_JSON);
    }

    @Override
    public void write(OutputStream outputStream) throws IOException {
        setCharacterSet(CharacterSet.UTF_8);
        generator = new JsonFactory().createJsonGenerator(outputStream, JsonEncoding.UTF8);
        try {
            generate();
        } finally {
            generator.close();
        }
    }

    protected abstract void generate() throws IOException;
}
