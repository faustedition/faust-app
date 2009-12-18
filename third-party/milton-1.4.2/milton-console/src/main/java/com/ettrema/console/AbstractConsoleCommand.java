
package com.ettrema.console;

import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.util.List;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractConsoleCommand implements ConsoleCommand{
    private static final Logger log = LoggerFactory.getLogger(AbstractConsoleCommand.class);
    
    protected final List<String> args;
    protected final Cursor cursor;
    protected final ResourceFactory resourceFactory;

    AbstractConsoleCommand(List<String> args, String host, String currentDir, ResourceFactory resourceFactory) {
        this.args = args;
        cursor = new Cursor( resourceFactory, host, currentDir );
        this.resourceFactory = resourceFactory;
    }    
    
    /**
     * The current resource must be a collection
     *
     * @return
     */
    protected CollectionResource currentResource() {
        return (CollectionResource) cursor.getResource();
    }
    
    protected Result result(String msg) {
        return new Result(cursor.getPath().toString(),msg);
    }    
    
    protected Resource host() {
        return cursor.host();
    }    


}
