package de.faustedition;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closer;
import org.junit.Test;

import java.io.InputStream;
import java.net.URL;

public class SSLTest {

    @Test
    public void connectViaHttps() throws Exception {
        final Closer closer = Closer.create();
        try {
            InputStream stream = closer.register(new URL("https://faustedition.uni-wuerzburg.de/wiki/").openStream());
            ByteStreams.copy(stream, ByteStreams.nullOutputStream());
        } finally {
            closer.close();
        }
    }
}
