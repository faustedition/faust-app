
package com.ettrema.console;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.PutableResource;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;

public class Mk extends AbstractConsoleCommand {

    public Mk(List<String> args, String host, String currentDir, ConsoleResourceFactory resourceFactory) {
        super(args, host, currentDir, resourceFactory);
    }

    public Result execute() {
        String newName = args.get(0);
        if( newName == null || newName.length() == 0 ) return result("Please enter a new file name");
        String content = "";
        if( args.size() > 1 ) content = args.get(1);
        ByteArrayInputStream inputStream = new ByteArrayInputStream( content.getBytes());

        if( !cursor.isFolder() ) {
            return result("Couldnt find current folder");
        }
        CollectionResource cur = (CollectionResource) cursor.getResource();
        if( cur.child(newName) != null ) return result("File already exists: " + newName);

        if( cur instanceof PutableResource ) {
            PutableResource putable = (PutableResource) cur;
            try {
                putable.createNew( newName, inputStream, (long) content.length(), newName );
                Path newPath = cursor.getPath().child( newName );
                return result( "created <a href='" + newPath + "'>" + newName + "</a>");
            } catch( IOException ex ) {
                return result("IOException writing content");
            }
        } else {
            return result("the folder doesnt support creating new items");
        }
    }

}
