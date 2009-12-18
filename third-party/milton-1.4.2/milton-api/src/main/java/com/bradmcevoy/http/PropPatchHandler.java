package com.bradmcevoy.http;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;
import org.xml.sax.XMLReader;
import org.xml.sax.helpers.XMLReaderFactory;

import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Response.Status;
import com.bradmcevoy.http.XmlWriter.Element;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;

/**
 * Example request (from ms office)
 *
 * PROPPATCH /Documents/test.docx HTTP/1.1
content-length: 371
cache-control: no-cache
connection: Keep-Alive
host: milton:8080
user-agent: Microsoft-WebDAV-MiniRedir/6.0.6001
pragma: no-cache
translate: f
if: (<opaquelocktoken:900f718e-801c-4152-ae8e-f9395fe45d71>)
content-type: text/xml; charset="utf-8"
<?xml version="1.0" encoding="utf-8" ?>
 * <D:propertyupdate xmlns:D="DAV:" xmlns:Z="urn:schemas-microsoft-com:">
 *  <D:set>
 *  <D:prop>
 *  <Z:Win32LastAccessTime>Wed, 10 Dec 2008 21:55:22 GMT</Z:Win32LastAccessTime>
 *  <Z:Win32LastModifiedTime>Wed, 10 Dec 2008 21:55:22 GMT</Z:Win32LastModifiedTime>
 *  <Z:Win32FileAttributes>00000020</Z:Win32FileAttributes>
 * </D:prop>
 * </D:set>
 * </D:propertyupdate>
 *
 *
 * And another example request (from spec)
 *
 *    <?xml version="1.0" encoding="utf-8" ?>
<D:propertyupdate xmlns:D="DAV:"
xmlns:Z="http://www.w3.com/standards/z39.50/">
<D:set>
<D:prop>
<Z:authors>
<Z:Author>Jim Whitehead</Z:Author>
<Z:Author>Roy Fielding</Z:Author>
</Z:authors>
</D:prop>
</D:set>
<D:remove>
<D:prop><Z:Copyright-Owner/></D:prop>
</D:remove>
</D:propertyupdate>

 *
 *
 * Here is an example response (from the spec)
 *
 *    HTTP/1.1 207 Multi-Status
Content-Type: text/xml; charset="utf-8"
Content-Length: xxxx

<?xml version="1.0" encoding="utf-8" ?>
<D:multistatus xmlns:D="DAV:" xmlns:Z="http://www.w3.com/standards/z39.50">
<D:response>
<D:href>http://www.foo.com/bar.html</D:href>
<D:propstat>
<D:prop><Z:Authors/></D:prop>
<D:status>HTTP/1.1 424 Failed Dependency</D:status>
</D:propstat>
<D:propstat>
<D:prop><Z:Copyright-Owner/></D:prop>
<D:status>HTTP/1.1 409 Conflict</D:status>
</D:propstat>
<D:responsedescription> Copyright Owner can not be deleted or altered.</D:responsedescription>
</D:response>
</D:multistatus>

 *
 *
 * @author brad
 */
public class PropPatchHandler extends ExistingEntityHandler {

    private final static Logger log = LoggerFactory.getLogger( PropPatchHandler.class );
    private static final String CUSTOM_NS_PREFIX = "R";

    PropPatchHandler( HttpManager manager ) {
        super( manager );
    }

    public Request.Method method() {
        return Method.PROPPATCH;
    }

    protected boolean isCompatible( Resource handler ) {
        return ( handler instanceof PropPatchableResource );
    }

    protected void process( HttpManager milton, Request request, Response response, Resource resource ) {
        log.debug( "process" );

       	if( isLockedOut(request, resource))
    	{
    		response.setStatus(Status.SC_LOCKED);
    		return;
    	}
 
        PropPatchableResource patchable = (PropPatchableResource) resource;
        // todo: check if token header
        try {
            InputStream in = request.getInputStream();
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            StreamUtils.readTo( in, out );
            System.out.println( "PropPatch: " + out.toString() );
            Fields fields = parseContent( out.toByteArray() );
            if( fields.size() > 0 ) {
                patchable.setProperties( fields );
            }
            respondOk( request, response, fields, patchable );
        } catch( SAXException ex ) {
            throw new RuntimeException( ex );
        } catch( WritingException ex ) {
            throw new RuntimeException( ex );
        } catch( ReadingException ex ) {
            throw new RuntimeException( ex );
        } catch( IOException ex ) {
            throw new RuntimeException( ex );
        }
    }

    static Fields parseRequest( Request request ) throws IOException, SAXException {
        InputStream in = request.getInputStream();
        return parseContent( in );
    }

    static Fields parseContent( InputStream in ) throws IOException, SAXException {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        StreamUtils.readTo( in, bout, false, true );
        byte[] arr = bout.toByteArray();
        return parseContent( arr );
    }

    static Fields parseContent( byte[] arr ) throws IOException, SAXException {
        if( arr.length == 0 ) {
            System.out.println( "no content for proppatch fields" );
            return new Fields();
        } else {
            ByteArrayInputStream bin = new ByteArrayInputStream( arr );
            XMLReader reader = XMLReaderFactory.createXMLReader();
            PropPatchSaxHandler handler = new PropPatchSaxHandler();
            reader.setContentHandler( handler );
            reader.parse( new InputSource( bin ) );
            Fields fields = handler.getFields();
            if( fields == null ) fields = new Fields();
            return fields;
        }

    }

    private void sendResponse( XmlWriterFieldConsumer consumer, Fields fields, String href, PropPatchableResource resource ) {
        consumer.consumeProperties( fields, href, resource );


    }

    public static class Field {

        public final String name;
        String namespaceUri;

        public Field( String name ) {
            this.name = name;
        }

        public void setNamespaceUri( String namespaceUri ) {
            this.namespaceUri = namespaceUri;
        }

        public String getNamespaceUri() {
            return namespaceUri;
        }
    }

    public static class SetField extends Field {

    	public final String value;

        public SetField( String name, String value ) {
            super( name );
            this.value = value;
        }
    }

    public static class Fields implements Iterable<Field> {

        /**
         * fields to remove
         */
       public  final List<Field> removeFields = new ArrayList<Field>();
        /**
         * fields to set to a value
         */
        public final List<SetField> setFields = new ArrayList<PropPatchHandler.SetField>();

        private int size() {
            return removeFields.size() + setFields.size();
        }

        public Iterator<Field> iterator() {
            List<Field> list = new ArrayList<Field>( removeFields );
            list.addAll( setFields );
            return list.iterator();
        }

        private boolean isEmpty() {
            return size() == 0;
        }
    }

    private void respondOk( Request request, Response response, Fields fields, PropPatchableResource resource ) {
        log.debug( "respondOk" );
        response.setStatus( Response.Status.SC_OK );

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        XmlWriter writer = new XmlWriter( out );
        XmlWriterFieldConsumer consumer = new XmlWriterFieldConsumer( writer );
        writer.writeXMLHeader();
        writer.open( "D:multistatus" + generateNamespaceDeclarations() );
        writer.newLine();
        String url = request.getAbsoluteUrl();
        appendResponses( consumer, fields, url, resource );
        writer.close( "D:multistatus" );
        writer.flush();
        log.debug( out.toString() );
        String xml = toString( out );
        writeXml( response, xml );

    }

    private void appendResponses( XmlWriterFieldConsumer consumer, Fields fields, String url, PropPatchableResource resource ) {
        log.debug( "appendresponses: fields size: " + fields.size() );
        try {
            String collectionHref = url;
            URI parentUri = new URI( collectionHref );

            collectionHref = parentUri.toASCIIString();
            sendResponse( consumer, fields, collectionHref, resource );

        } catch( URISyntaxException ex ) {
            throw new RuntimeException( ex );
        }
    }

    protected String generateNamespaceDeclarations() {
//            return " xmlns:" + nsWebDav.abbrev + "=\"" + nsWebDav.url + "\"";
        return " xmlns:D" + "=\"DAV:\"";
    }

    private String toString( ByteArrayOutputStream out ) {
        try {
            return out.toString( "UTF-8" );
        } catch( UnsupportedEncodingException ex ) {
            throw new RuntimeException( ex );
        }
    }

    private void writeXml( Response response, String xml ) {
        try {
            response.getOutputStream().write( xml.getBytes() );
        } catch( IOException ex ) {
            log.warn( "exception writing response. " + ex );
        }
    }

    /**
     * Copied from PropFindHandler. TODO: extract and make common
     */
    class XmlWriterFieldConsumer {

        final XmlWriter writer;

        public XmlWriterFieldConsumer( XmlWriter writer ) {
            this.writer = writer;
        }

        public void startResource( String href ) {
            writeProp( "D:href", href );
        }

        public Element open( String elementName ) {
            return writer.begin( elementName ).open();
        }

        void writeProp( String elementName, String value ) {
            writer.writeProperty( null, elementName, value );
        }

        public void consumeProperties( Fields fields, String href, PropPatchableResource resource ) {
            XmlWriter.Element el = writer.begin( "D:response" );
            if( resource instanceof CustomPropertyResource ) {
                CustomPropertyResource cpr = (CustomPropertyResource) resource;
                el.writeAtt( "xmlns:" + CUSTOM_NS_PREFIX, cpr.getNameSpaceURI() );
            }
            el.open();
            startResource( href );
            sendResponseProperties( resource, fields, href, "HTTP/1.1 200 Ok" );
            el.close();
        }

        void sendResponseProperties( PropPatchableResource resource, Fields fields, String href, String status ) {
            if( !fields.isEmpty() ) {
                XmlWriter.Element elPropStat = writer.begin( "D:propstat" ).open();
                XmlWriter.Element elProp = writer.begin( "D:prop" ).open();
                for( final Field field : fields ) {
                    String fieldName = "Z:" + field.name;
                    writeProp( fieldName, null );
                }
                elProp.close();
                writeProp( "D:status", status );
                elPropStat.close();
            }
        }
    }
}
