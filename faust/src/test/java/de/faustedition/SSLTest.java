package de.faustedition;

import java.io.InputStream;
import java.net.URL;

import org.junit.Test;

import com.google.common.io.ByteStreams;
import com.google.common.io.Closeables;
import com.google.common.io.NullOutputStream;

public class SSLTest {

    @Test
    public void connectViaHttps() throws Exception {
        InputStream stream = new URL("https://faustedition.uni-wuerzburg.de/wiki/").openStream();
        ByteStreams.copy(stream, new NullOutputStream());
        Closeables.closeQuietly(stream);
    }
}
