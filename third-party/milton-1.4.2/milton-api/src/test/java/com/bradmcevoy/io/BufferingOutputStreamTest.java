package com.bradmcevoy.io;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import junit.framework.TestCase;

/**
 *
 * @author brad
 */
public class BufferingOutputStreamTest extends TestCase {
    
    public BufferingOutputStreamTest(String testName) {
        super(testName);
    }

    public void testWriteByte() throws Exception {
        BufferingOutputStream out = new BufferingOutputStream( 10);
        assertNull(out.getTempFile());
        out.write( 1);
        assertNull(out.getTempFile());
        assertEquals( 1, out.getTempMemoryBuffer().size());
    }

    public void test_WriteArray() throws Exception {
        BufferingOutputStream out = new BufferingOutputStream( 10);
        out.write( new byte[5]);
        assertEquals( 5, out.getTempMemoryBuffer().size());

        out.write( new byte[5],1,2);
        assertEquals( 7, out.getTempMemoryBuffer().size());
    }

    public void test_MemoryRead() throws Exception {
        BufferingOutputStream out = new BufferingOutputStream( 10);
        out.write( new byte[9]);
        assertNotNull( out.getTempMemoryBuffer());
        assertNull( out.getTempFile());

        out.close();

        InputStream in = out.getInputStream();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        StreamUtils.readTo( in, out2 );
        byte[] arr = out2.toByteArray();
        assertEquals( 9, arr.length);

    }

    public void test_TransitionToFile() throws Exception {
        BufferingOutputStream out = new BufferingOutputStream( 10);
        out.write( new byte[10]);
        assertNull( out.getTempMemoryBuffer());
        assertNotNull( out.getTempFile());

        out.close();
        InputStream in = out.getInputStream();
        ByteArrayOutputStream out2 = new ByteArrayOutputStream();
        StreamUtils.readTo( in, out2 );
        byte[] arr = out2.toByteArray();
        assertEquals( 10, arr.length);
    }

}
