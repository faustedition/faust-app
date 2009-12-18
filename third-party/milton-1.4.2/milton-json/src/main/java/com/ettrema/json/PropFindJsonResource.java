package com.ettrema.json;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PropFindHandler;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import net.sf.json.JsonConfig;
import net.sf.json.util.CycleDetectionStrategy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PropFindJsonResource implements GetableResource {

    private static final Logger log = LoggerFactory.getLogger( PropFindJsonResource.class );
    private final PropFindableResource wrappedResource;
    private final PropFindHandler propFindHandler;
    private final String encodedUrl;

    public PropFindJsonResource( PropFindableResource wrappedResource, PropFindHandler propFindHandler, String encodedUrl ) {
        super();
        this.wrappedResource = wrappedResource;
        this.propFindHandler = propFindHandler;
        this.encodedUrl = encodedUrl;
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException {
        JsonConfig cfg = new JsonConfig();
        cfg.setCycleDetectionStrategy( CycleDetectionStrategy.LENIENT );

        JSON json;
        Writer writer = new PrintWriter( out );
        String[] arr;
        if( propFindHandler == null ) {
            if( wrappedResource instanceof CollectionResource ) {
                List<? extends Resource> children = ( (CollectionResource) wrappedResource ).getChildren();
                json = JSONSerializer.toJSON( toSimpleList( children ), cfg );
            } else {
                json = JSONSerializer.toJSON( toSimple( wrappedResource ), cfg );
            }
        } else {
            // use propfind handler
            String sFields = params.get( "fields" );
            Set<String> fields = new HashSet<String>();
            if( sFields != null && sFields.length() > 0 ) {
                arr = sFields.split( "," );
                for( String s : arr ) {
                    fields.add( s.trim() );
                }
            }

            String sDepth = params.get( "depth" );
            int depth = 1;
            if( sDepth != null && sDepth.trim().length() > 0 ) {
                depth = Integer.parseInt( sDepth );
            }

            MapBuildingPropertyConsumer consumer = new MapBuildingPropertyConsumer();
            String href = encodedUrl.replace( "/_DAV/PROPFIND", "");
            log.debug( "href: " + href);
            propFindHandler.appendResponses( consumer, wrappedResource, depth, fields, href );
            json = JSONSerializer.toJSON( consumer.getProperties(), cfg );
        }
        json.write( writer );
        writer.flush();
    }

    private List<SimpleResource> toSimpleList( List<? extends Resource> list ) {
        List<SimpleResource> simpleList = new ArrayList<SimpleResource>( list.size() );
        for( Resource r : list ) {
            simpleList.add( toSimple( r ) );
        }
        return simpleList;
    }

    private SimpleResource toSimple( Resource r ) {
        return new SimpleResource( r );
    }

    public Long getMaxAgeSeconds( Auth auth ) {
        return 0L;
    }

    public String getContentType( String accepts ) {
        return "application/json";
    }

    public Long getContentLength() {
        return null;
    }

    public String getUniqueId() {
        return null;
    }

    public String getName() {
        return Request.Method.PROPFIND.code;
    }

    public Object authenticate( String user, String password ) {
        return wrappedResource.authenticate( user, password );
    }

    public boolean authorise( Request request, Method method, Auth auth ) {
        return wrappedResource.authorise( request, Request.Method.PROPFIND, auth );
    }

    public String getRealm() {
        return wrappedResource.getRealm();
    }

    public Date getModifiedDate() {
        return null;
    }

    public String checkRedirect( Request request ) {
        return null;
    }

    public class SimpleResource {

        private final Resource r;

        public SimpleResource( Resource r ) {
            this.r = r;
        }

        public String getName() {
            return r.getName();
        }

        public Date getModifiedDate() {
            return r.getModifiedDate();
        }
    }
}
