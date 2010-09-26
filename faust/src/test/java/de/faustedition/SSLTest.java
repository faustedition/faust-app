package de.faustedition;

import java.io.InputStream;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.junit.Test;

public class SSLTest {

    @Test
    public void connectViaHttps() throws Exception {
        InputStream stream = new URL("https://faustedition.uni-wuerzburg.de/wiki/").openStream();
        IOUtils.toByteArray(stream);
        stream.close();
    }
}
