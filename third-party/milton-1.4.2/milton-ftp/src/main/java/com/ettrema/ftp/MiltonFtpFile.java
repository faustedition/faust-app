////////////////////////////////////////////////////////////////////////////////
//
// Copyright (c) 2009, Suncorp Metway Limited. All rights reserved.
//
// This is unpublished proprietary source code of Suncorp Metway Limited.
// The copyright notice above does not evidence any actual or intended
// publication of such source code.
//
////////////////////////////////////////////////////////////////////////////////
package com.ettrema.ftp;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.MakeCollectionableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.PutableResource;
import com.bradmcevoy.http.ReplaceableResource;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.ConflictException;
import com.bradmcevoy.http.exceptions.NotAuthorizedException;
import com.bradmcevoy.io.BufferingOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import org.apache.ftpserver.ftplet.FtpFile;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Adapter to between apache FTP's FtpFile interface and milton's Resource interface
 *
 * This class can wrap any Resource instance and will allow or disallow requests
 * as appropriate given what methods the resource instance supports - Eg DeletableResource,
 * MoveableResource, etc
 *
 * @author u370681
 */
public class MiltonFtpFile implements FtpFile {

    private static final Logger log = LoggerFactory.getLogger( MiltonFtpFile.class );
    private final Path path;
    private CollectionResource parent;
    private final MiltonFsView ftpFactory;
    private Resource r;
    private final MiltonUser user;

    public MiltonFtpFile( MiltonFsView resourceFactory, Path path, Resource r, MiltonUser user ) {
        this.path = path;
        this.r = r;
        this.parent = null;
        this.ftpFactory = resourceFactory;
        this.user = user;
    }

    public MiltonFtpFile( MiltonFsView resourceFactory, Path path, CollectionResource parent, Resource r, MiltonUser user ) {
        this.path = path;
        this.r = null;
        this.parent = parent;
        this.ftpFactory = resourceFactory;
        this.user = user;
    }

    public String getAbsolutePath() {
        return path.toString();
    }

    public String getName() {
        return r.getName();
    }

    public boolean isHidden() {
        return false;
    }

    public boolean isDirectory() {
        return ( r instanceof CollectionResource );
    }

    public boolean isFile() {
        return !isDirectory();
    }

    public boolean doesExist() {
        return r != null;
    }

    public boolean isReadable() {
        log.debug( "isReadble" );
        if( r == null || !( r instanceof GetableResource ) ) return false;

        Auth auth = new Auth( user.getName(), user.getUser() );
        FtpRequest request = new FtpRequest( Method.GET, auth, path.toString() );
        return r.authorise( request, request.getMethod(), auth );

    }

    /**
     * Check file write permission.
     */
    public boolean isWritable() {
        log.debug( "isWritable: " + getAbsolutePath() );
        if( path.isRoot() ) return false;
        Auth auth = new Auth( user.getName(), user.getUser() );
        FtpRequest request = new FtpRequest( Method.DELETE, auth, path.toString() );
        if( r != null ) {
            if( r instanceof ReplaceableResource ) {
                return r.authorise( request, Method.PUT, auth );
            }
        }
        if( getParent() instanceof PutableResource ) {
            return getParent().authorise( request, Method.PUT, auth );
        } else {
            return false;
        }
    }

    public boolean isRemovable() {
        log.debug( "isRemovable: " + getAbsolutePath() );
        if( r == null ) return false;
        if( path.isRoot() ) return false;
        Auth auth = new Auth( user.getName(), user.getUser() );
        FtpRequest request = new FtpRequest( Method.DELETE, auth, path.toString() );
        boolean b = r.authorise( request, Method.DELETE, auth );
        log.debug( ".. = " + b );
        return b;
    }

    public String getOwnerName() {
        return "anyone";
    }

    public String getGroupName() {
        return "anygroup";
    }

    public int getLinkCount() {
        return 0;
    }

    public long getLastModified() {
        return r.getModifiedDate().getTime();
    }

    public boolean setLastModified( long time ) {
        return false;
    }

    public long getSize() {
        if( r instanceof GetableResource ) {
            GetableResource gr = (GetableResource) r;
            Long ll = gr.getContentLength();
            if( ll == null ) return 0;
            return ll.longValue();
        } else {
            return 0;
        }
    }

    public boolean mkdir() {
        log.debug( "mkdir: " + this.path );
        if( parent != null ) {
            if( parent instanceof MakeCollectionableResource ) {
                MakeCollectionableResource mcr = (MakeCollectionableResource) parent;
                try {
                    r = mcr.createCollection( path.getName() );
                    return true;
                } catch( NotAuthorizedException ex ) {
                    log.debug( "no authorised" );
                    return false;
                } catch( ConflictException ex ) {
                    log.debug( "conflict" );
                    return false;
                }
            } else {
                log.debug( "parent does not support creating collection" );
                return false;
            }
        } else {
            throw new RuntimeException( "no parent" );
        }
    }

    public boolean delete() {
        if( r instanceof DeletableResource ) {
            DeletableResource dr = (DeletableResource) r;
            dr.delete();
            return true;
        } else {
            return false;
        }
    }

    public boolean move( FtpFile newFile ) {
        if( r == null ) {
            throw new RuntimeException( "resource not saved yet" );
        } else if( r instanceof MoveableResource ) {
                MoveableResource src = (MoveableResource) r;
                MiltonFtpFile dest = (MiltonFtpFile) newFile;
                CollectionResource crDest;
                crDest = dest.getParent();
                String newName = dest.path.getName();
                try {
                    src.moveTo( crDest, newName );
                    return true;
                } catch( ConflictException ex ) {
                    log.error( "can't move", ex );
                    return false;
                }
            } else {
                log.debug( "not moveable: " + this.getName() );
                return false;
            }
    }

    public List<FtpFile> listFiles() {
        log.debug( "listfiles" );
        List<FtpFile> list = new ArrayList<FtpFile>();
        if( r instanceof CollectionResource ) {
            CollectionResource cr = (CollectionResource) r;
            for( Resource child : cr.getChildren() ) {
                list.add( ftpFactory.wrap( path.child( child.getName() ), child ) );
            }
        }
        return list;
    }

    public OutputStream createOutputStream( long offset ) throws IOException {
        log.debug( "createOutputStream: " + offset );
        final BufferingOutputStream out = new BufferingOutputStream( 50000 );
        if( r instanceof ReplaceableResource ) {
            log.debug( "resource is replaceable" );
            final ReplaceableResource rr = (ReplaceableResource) r;
            Runnable runnable = new Runnable() {

                public void run() {
                    rr.replaceContent( out.getInputStream(), out.getSize() );
                }
            };
            out.setOnClose( runnable );
            return out;
        } else {
            CollectionResource col;
            col = getParent();
            if( col == null ) {
                throw new IOException( "parent not found" );
            } else if( col instanceof PutableResource ) {
                    final PutableResource putableResource = (PutableResource) col;
                    final String newName = path.getName();
                    Runnable runnable = new Runnable() {

                        public void run() {
                            try {
                                putableResource.createNew( newName, out.getInputStream(), out.getSize(), null );
                            } catch( IOException ex ) {
                                throw new RuntimeException( ex );
                            }
                        }
                    };
                    out.setOnClose( runnable );
                    return out;
                } else {
                    throw new IOException( "folder doesnt support PUT, and the resource is not replaceable" );
                }
        }
    }

    public InputStream createInputStream( long offset ) throws IOException {
        if( r instanceof GetableResource ) {
            GetableResource gr = (GetableResource) r;
            String ct = gr.getContentType( null );
            BufferingOutputStream out = new BufferingOutputStream( 50000 );
            try {
                gr.sendContent( out, null, null, ct );
                out.close();
                return out.getInputStream();
            } catch( NotAuthorizedException ex ) {
                log.warn( "not authorising", ex );
                return null;
            }
        } else {
            return null;
        }
    }

    private CollectionResource getParent() {
        if( parent == null ) {
            MiltonFsView.ResourceAndPath rp = ftpFactory.getResource( path.getParent() );
            if( rp.resource == null ) {
                throw new RuntimeException( "couldnt find parent: " + path );
            } else {
                parent = (CollectionResource) rp.resource;
            }
        }
        return parent;
    }
}
