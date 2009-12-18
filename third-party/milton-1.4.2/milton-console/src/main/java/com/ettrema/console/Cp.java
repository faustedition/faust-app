package com.ettrema.console;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.CopyableResource;
import com.bradmcevoy.http.Resource;
import java.util.List;

public class Cp extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Cp.class );

    Cp( List<String> args, String host, String currentDir, ConsoleResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    public Result execute() {
        String srcPath = args.get( 0 );
        String destPath = args.get( 1 );
        log.debug( "copy: " + srcPath + "->" + destPath );

        Path pSrc = Path.path( srcPath );
        Path pDest = Path.path( destPath );

        Cursor sourceCursor = cursor.find( pSrc );
        Cursor destCursor = cursor.find( pDest);
        if( !sourceCursor.exists()) {
            List<Resource> list = sourceCursor.getParent().childrenWithFilter( sourceCursor.getPath().getName());
            if( list != null ) {
                if( destCursor.isFolder()) {
                    return copyTo( list, (CollectionResource)destCursor.getResource());
                } else {
                    return result("destination is not a folder: " + pDest);
                }
            } else {
                return result("source is not found: " + pSrc);
            }
        } else if( sourceCursor.isFolder() ) {
            if( destCursor.exists() ) {
                if( destCursor.isFolder()) {
                    // copy folder to existing folder, so just copy contents of source
                    return doCopyChildren(sourceCursor.getResource(), destCursor.getResource());
                } else {
                    // dest exists, but is not a folder, so can't overwrite
                    return result("destination folder already exists: " + pDest);
                }
            } else {
                Cursor destParent = destCursor.getParent();
                if( !destParent.exists() ) {
                    return result("The destination folder does not exist: " + destParent.getPath());
                } else if(!destParent.isFolder()) {
                    return result("The destination parent is not a folder (somehow)" + destParent.getPath());
                } else {
                    return doCopy(sourceCursor.getResource(), destParent.getResource(), destCursor.getPath().getName());
                }

            }
        } else {
            log.debug( "is a single file copy. dest must not exist or be a folder");
            if( destCursor.exists() ) {
                if( destCursor.isFolder()) {
                    return doCopy(sourceCursor.getResource(), destCursor.getResource());
                } else {
                    return result("destination already exists: " + pDest);
                }
            } else {
                log.debug( "copying to parent..");
                Cursor destParent = destCursor.getParent();
                if( !destParent.exists() ) {
                    return result("The destination folder does not exist: " + destParent.getPath());
                } else if(!destParent.isFolder()) {
                    return result("The destination parent is not a folder (somehow)" + destParent.getPath());
                } else {
                    return doCopy(sourceCursor.getResource(), destParent.getResource(), destCursor.getPath().getName());
                }
            }
        }
    }

    private Result doCopy( Resource src, Resource dest ) {
        if( dest == null) throw new IllegalArgumentException( "dest is null");
        return doCopy( src, dest, src.getName());
    }

    private Result doCopy( Resource src, Resource dest, String name ) {
        if( src instanceof CopyableResource ) {
            CopyableResource cr = (CopyableResource) src;
            if( dest instanceof CollectionResource ) {
                CollectionResource destFolder = (CollectionResource) dest;
                cr.copyTo( destFolder, name );
                return result("Copied to: " + dest.getName());
            } else {
                return result("The destination is not a folder: " + dest.getName());
            }
        } else {
            return result("the source file is not copyable");
        }
    }

    private Result doCopyChildren( Resource currentResource, Resource destResource ) {
        CollectionResource src = (CollectionResource) currentResource;
        CollectionResource dest = (CollectionResource) destResource;
        return copyTo( src.getChildren(), dest );
    }

    protected Result copyTo( List<? extends Resource> list, CollectionResource destFolder ) {
        log.debug( "copyTo: " + list.size() + " -> " + destFolder.getName() );
        for( Resource res : list ) {
            log.debug( "copying: " + res.getName() );
            if( res instanceof CopyableResource ) {
                CopyableResource cr = (CopyableResource) res;
                cr.copyTo( destFolder, cr.getName() );
            }
        }
        return result( "Copied to: " + destFolder.getName() );
    }
}
