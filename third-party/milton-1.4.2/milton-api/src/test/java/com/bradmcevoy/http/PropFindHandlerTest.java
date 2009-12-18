/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.bradmcevoy.http;

import com.bradmcevoy.http.Response.Status;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import junit.framework.TestCase;

import org.jdom.Document;
import org.jdom.input.SAXBuilder;
import static org.easymock.EasyMock.*;

/**
 *
 * @author brad
 */
public class PropFindHandlerTest extends TestCase {

    PropFindHandler handler;
    Request request;
    Response response;
    PropFindableResource pfr;
    CustomPropertyResource cpr;
    CustomProperty prop;
    String namespace = "http://ns.example.com/boxschema/";
    Date aDate;

    public PropFindHandlerTest(String testName) {
        super(testName);
    }

    @Override
    protected void setUp() throws Exception {
        handler = new PropFindHandler( null);
        request = createMock( Request.class);
        response = createMock( Response.class);
        pfr = createMock( PropFindableResource.class);
        cpr = createMock( CustomPropertyResource.class);
        prop = createMock( CustomProperty.class);
        aDate = SimpleDateFormat.getInstance().parse( "11/4/03 8:14 PM");
    }



    public void testNoPropsSpecified() throws Exception {
        prepareRequest("");

        expect(pfr.getName()).andReturn( "test");
        expect(pfr.getCreateDate()).andReturn( aDate ).atLeastOnce();
        expect(pfr.getModifiedDate()).andReturn( aDate ).atLeastOnce();
        expect(pfr.getUniqueId()).andReturn( "abc" ).atLeastOnce();
        replay(pfr);
        ByteArrayOutputStream out = prepareResponse();

        handler.process( null, request, response, pfr);

        System.out.println( "----- response ----" );
        System.out.println( out.toString() );
        checkValidXml( out.toString());
    }

    public void testSinglePropOnCustomerPropertyResource() throws Exception {
        String xml = "<?xml version=\"1.0\" encoding=\"utf-8\" ?> " +
                    "<D:propfind xmlns:D=\"DAV:\">" +
                    "<D:prop xmlns:R=\"" + namespace + "\">" +
                    "<R:author/> " +
                    "</D:prop> " +
                    "</D:propfind>";
        prepareRequest(xml);
        ByteArrayOutputStream out = prepareResponse();

        expect(cpr.getNameSpaceURI()).andReturn( namespace );
        expect(cpr.getProperty( "author")).andReturn( prop);
        replay(cpr);

        expect(prop.getFormattedValue()).andReturn( "abcä<"); // include awkward characters for encoding
        replay(prop);

        handler.process( null, request, response, cpr);

        System.out.println( out.toString() );

        verify(prop, cpr);
        checkValidXml( out.toString("UTF-8"));
    }

    private void prepareRequest(String xml) throws IOException {
        expect( request.getDepthHeader() ).andReturn( 1 ).atLeastOnce();
        expect( request.getInputStream() ).andReturn( new ByteArrayInputStream( xml.getBytes() ) );
        expect( request.getAbsoluteUrl() ).andReturn( "http://www.blah.com/test" ).atLeastOnce();
        replay( request );
    }

    private ByteArrayOutputStream prepareResponse() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        expect( response.getOutputStream() ).andReturn( out );
        response.setContentTypeHeader( "text/xml; charset=UTF-8" );
        expectLastCall();
        response.setStatus( Status.SC_MULTI_STATUS );
        expectLastCall();
        replay( response );
        return out;
    }

    void checkValidXml(String xml) throws Exception {
        ByteArrayInputStream in = new ByteArrayInputStream( xml.getBytes());
        org.jdom.input.SAXBuilder b = new SAXBuilder();
        Document doc = b.build( in );
    }
}
