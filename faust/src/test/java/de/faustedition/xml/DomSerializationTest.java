package de.faustedition.xml;

import java.io.IOException;

import javax.xml.transform.TransformerException;

import org.junit.Test;
import org.xml.sax.SAXException;

public class DomSerializationTest {

    @Test
    public void domSerializer() throws IOException, TransformerException, SAXException {
        XMLUtil.serialize(XMLUtil.parse(getClass().getResourceAsStream("/de/faustedition/tei/faust-tei-sample.xml")), System.out);
    }
}
