package com.ettrema.http.fs;

import com.bradmcevoy.common.ContentTypeUtils;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class FsFileResource extends FsResource implements CopyableResource, DeletableResource, GetableResource, MoveableResource, PropFindableResource {

    private static final Logger log = LoggerFactory.getLogger( FsFileResource.class );

    public FsFileResource( FileSystemResourceFactory factory, File file ) {
        super( factory, file );
    }

    public Long getContentLength() {
        return file.length();
    }

    public String getContentType( String preferredList ) {
        String mime = ContentTypeUtils.findContentTypes( this.file );
        return ContentTypeUtils.findAcceptableContentType( mime, preferredList );
    }

    public String checkRedirect( Request arg0 ) {
        return null;
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException {
        log.debug( "send content: " + file.getAbsolutePath() );
        FileInputStream in = null;
        try {
            in = new FileInputStream( file );
            //        if( range != null ) {
            //            long start = range.getStart();
            //            if( start > 0 ) in.skip(start);
            //            long finish = range.getFinish();
            //            if( finish > 0 ) {
            //                StreamToStream.readTo(in, out);
            //            }
            //        } else {
            int bytes = IOUtils.copy( in, out );
            log.debug( "wrote bytes:  " + bytes );
            out.flush();
        //        }
        } finally {
            IOUtils.closeQuietly( in );
        }
    }

    /** @{@inheritDoc} */
    public Long getMaxAgeSeconds(Auth auth) {
        return factory.maxAgeSeconds( this );
    }

    /** @{@inheritDoc} */
    @Override
    protected void doCopy( File dest ) {
        try {
            FileUtils.copyFile( file, dest );
        } catch( IOException ex ) {
            throw new RuntimeException( "Failed doing copy to: " + dest.getAbsolutePath(), ex );
        }
    }
}
