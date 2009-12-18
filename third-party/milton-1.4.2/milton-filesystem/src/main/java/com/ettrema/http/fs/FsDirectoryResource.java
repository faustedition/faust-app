package com.ettrema.http.fs;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.LockInfo;
import com.bradmcevoy.http.LockResult;
import com.bradmcevoy.http.LockTimeout;
import com.bradmcevoy.http.LockToken;
import com.bradmcevoy.http.LockingCollectionResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PropFindableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.XmlWriter;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 *
 */
public class FsDirectoryResource extends FsResource implements MakeCollectionableResource, PutableResource, CopyableResource, DeletableResource, MoveableResource, PropFindableResource, LockingCollectionResource, GetableResource {

    public FsDirectoryResource( FileSystemResourceFactory factory, File dir ) {
        super( factory, dir );
        if( !dir.exists() )
            throw new IllegalArgumentException( "Directory does not exist: " + dir.getAbsolutePath() );
        if( !dir.isDirectory() )
            throw new IllegalArgumentException( "Is not a directory: " + dir.getAbsolutePath() );
    }

    public CollectionResource createCollection( String name ) {
        File fnew = new File( file, name );
        boolean ok = fnew.mkdir();
        if( !ok )
            throw new RuntimeException( "Failed to create: " + fnew.getAbsolutePath() );
        return new FsDirectoryResource( factory, fnew );
    }

    public Resource child( String name ) {
        File fchild = new File( file, name );
        return factory.resolveFile( fchild );

    }

    public List<? extends Resource> getChildren() {
        ArrayList<FsResource> list = new ArrayList<FsResource>();
        File[] files = this.file.listFiles();
        if( files != null ) {
            for( File fchild : files ) {
                FsResource res = factory.resolveFile( fchild );
                list.add( res );
            }
        }
        return list;
    }

    public String checkRedirect( Request request ) {
        //return request.getAbsoluteUrl() + "/index.html";
        return null;
    }

    public Resource createNew( String name, InputStream in, Long length, String contentType ) throws IOException {
        File dest = new File( this.getFile(), name );
        FileOutputStream out = null;
        try {
            out = new FileOutputStream( dest );
            IOUtils.copy( in, out );
        } finally {
            IOUtils.closeQuietly( out );
        }
        // todo: ignores contentType
        return factory.resolveFile( dest );

    }

    @Override
    protected void doCopy( File dest ) {
        try {
            FileUtils.copyDirectory( this.getFile(), dest );
        } catch( IOException ex ) {
            throw new RuntimeException( "Failed to copy to:" + dest.getAbsolutePath(), ex );
        }
    }

    public LockToken createAndLock( String name, LockTimeout timeout, LockInfo lockInfo ) {
        File dest = new File( this.getFile(), name );
        createEmptyFile( dest );
        FsFileResource newRes = new FsFileResource( factory, dest );
        LockResult res = newRes.lock( timeout, lockInfo );
        return res.getLockToken();
    }

    private void createEmptyFile( File file ) {
        FileOutputStream fout = null;
        try {
            fout = new FileOutputStream( file );
        } catch( IOException e ) {
            throw new RuntimeException( e );
        } finally {
            IOUtils.closeQuietly( fout );
        }
    }

    public void sendContent( OutputStream out, Range range, Map<String, String> params, String contentType ) throws IOException, NotAuthorizedException {
        XmlWriter w = new XmlWriter( out );
        w.open( "html" );
        w.open( "body" );
        w.begin( "h1" ).open().writeText( this.getName() ).close();
        w.open( "table" );
        for( Resource r : getChildren() ) {
            w.open( "tr" );

            w.open( "td" );
            w.begin( "a" ).writeAtt( "href", r.getName() ).open().writeText( r.getName() ).close();
            w.close( "td" );

            w.begin( "td" ).open().writeText( r.getModifiedDate() + "" ).close();
            w.close( "tr" );
        }
        w.close( "table" );
        w.close( "body" );
        w.close( "html" );
        w.flush();
    }

    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    public String getContentType( String accepts ) {
        return "text/html";
    }

    public Long getContentLength() {
        return null;
    }
}
