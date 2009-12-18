package com.ettrema.json;

import com.bradmcevoy.http.CustomProperty;
import com.bradmcevoy.http.CustomPropertyResource;
import com.bradmcevoy.http.PropFindHandler;
import java.io.ByteArrayOutputStream;
import java.util.HashMap;
import java.util.Map;
import junit.framework.TestCase;

import static org.easymock.EasyMock.*;

/**
 *
 * @author brad
 */
public class PropFindJsonResourceTest extends TestCase {

    PropFindJsonResource jsonResource;
    CustomPropertyResource cpr;
    PropFindHandler propFindHandler;
    String encodedUrl;
    CustomProperty prop;

    public PropFindJsonResourceTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        cpr = createMock( CustomPropertyResource.class);
        propFindHandler = new PropFindHandler(null);
        encodedUrl = "http://www.blah.com/test";
        jsonResource = new PropFindJsonResource( cpr, propFindHandler, encodedUrl );
        prop = createMock( CustomProperty.class);
    }



    public void testSendContent() throws Exception {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        Map<String, String> params = new HashMap<String, String>();
        params.put( "fields", "author, name");

        expect(cpr.getProperty( "name")).andReturn( null);
        expect(cpr.getName()).andReturn( "test");
        expect(cpr.getProperty( "author")).andReturn( prop);
        replay(cpr);

        expect(prop.getTypedValue()).andReturn( "abcä<"); // include awkward characters for encoding
        replay(prop);


        jsonResource.sendContent( out, null, params, null);


        System.out.println( out.toString() );

        verify(cpr);
    }

}
