package com.ettrema.console;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.Resource;
import java.util.ArrayList;
import java.util.List;

/**
 *
 */
public class Rm extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Rm.class );

    public Rm( List<String> args, String host, String currentDir, ConsoleResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    @Override
    public Result execute() {
        String sPath = args.get( 0 );
        Path path = Path.path( sPath );

        Cursor sourceCursor = cursor.find( path );

        if( !sourceCursor.exists() ) {
            // try regex
            List<Resource> list = sourceCursor.getParent().childrenWithFilter( sourceCursor.getPath().getName() );
            if( list != null ) {
                return doDelete( list );
            } else {
                return result( "Not found: " + path );
            }
        } else {
            return doDelete( sourceCursor.getResource() );
        }
    }

    private Result delete( List<DeletableResource> deletables ) {
        StringBuffer sb = new StringBuffer( "deleted: " );
        for( DeletableResource dr : deletables ) {
            sb.append( dr.getName() ).append( ',' );
            dr.delete();
        }
        return result( sb.toString() );
    }

    private Result doDelete( List<Resource> list ) {
        List<DeletableResource> deletables = new ArrayList<DeletableResource>();
        for( Resource r : list ) {
            if( r instanceof DeletableResource ) {
                deletables.add( (DeletableResource) r );
            } else {
                return result( "Can't delete: " + r.getName() );
            }
        }
        if( deletables.size() > 0 ) {
            return delete( deletables );
        } else {
            return result( "No files found to delete" );
        }
    }

    private Result doDelete( Resource r ) {
        if( r instanceof DeletableResource ) {
            DeletableResource dr = (DeletableResource) r;
            dr.delete();
            return result( "deleted: " + r.getName() );
        } else {
            return result( "Can't delete: " + r.getName() );
        }

    }
}
