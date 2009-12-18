package com.ettrema.console;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.DeletableResource;
import com.bradmcevoy.http.MoveableResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.exceptions.ConflictException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 */
public class Rn extends AbstractConsoleCommand {

    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger( Rn.class );

    public Rn( List<String> args, String host, String currentDir, ConsoleResourceFactory resourceFactory ) {
        super( args, host, currentDir, resourceFactory );
    }

    @Override
    public Result execute() {
        String srcPath = args.get( 0 );
        String destName = args.get( 1 );
        log.debug( "rename: " + srcPath + "->" + destName );

        Path pSrc = Path.path( srcPath );

        Cursor sourceCursor = cursor.find( pSrc );
        Resource target = sourceCursor.getResource();

        if( target == null ) {
            log.debug("target not found: " + srcPath);
            return result("target not found: " + srcPath);
        } else {            
            if( target instanceof MoveableResource ) {
                try {
                    CollectionResource currentParent = (CollectionResource) sourceCursor.getParent().getResource();
                    MoveableResource mv = (MoveableResource) target;
                    mv.moveTo( currentParent, destName );
                    Cursor newCursor = sourceCursor.getParent().find( destName );
                    return result( "created: <a href='" + newCursor.getPath() + "'>" + destName + "</a>" );
                } catch( ConflictException ex ) {
                    return result("conflict: " + ex.getMessage());
                }
            } else {
                return result("resource is not moveable");
            }
        }
    }

}
