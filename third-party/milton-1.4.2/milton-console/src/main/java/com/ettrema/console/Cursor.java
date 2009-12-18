package com.ettrema.console;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.CollectionResource;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class Cursor {

    private static final Logger log = LoggerFactory.getLogger( Cursor.class );
    private final String host;
    private final ResourceFactory resourceFactory;
    private final Path path;
    private transient Resource current;
    private transient boolean currentLoaded;
    private transient String msg;

    public Cursor( ResourceFactory resourceFactory, String host, String sPath ) {
        this(resourceFactory, host, Path.path( sPath ) );
    }

    public Cursor( ResourceFactory resourceFactory, String host, Path path ) {
        this.resourceFactory = resourceFactory;
        this.host = host;
        this.path = path;
    }

    public Cursor( ResourceFactory resourceFactory, String host, Path path, Resource current, String msg ) {
        this.resourceFactory = resourceFactory;
        this.host = host;
        this.path = path;
        this.current = current;
        currentLoaded = true;
    }

    public Cursor getParent() {
        return new Cursor( resourceFactory, host, path.getParent());
    }

    public String getMessage() {
        return msg;
    }

    public boolean exists() {
        return getResource() != null;
    }

    public boolean isFolder() {
        return getResource() != null && (getResource() instanceof CollectionResource);
    }

    public Resource getResource() {
        if( !currentLoaded ) {
            current = resourceFactory.getResource( host, path.toString() );
            currentLoaded = true;
        }
        return current;
    }

    public Path getPath() {
        return path;
    }

    protected Resource host() {
        return resourceFactory.getResource( host, "/");
    }

    public Cursor find( String newPath ) {
        return find( Path.path( newPath ) );
    }


    public Cursor find( Path newPath ) {
        log.debug( "find: " + newPath);
        Path lastPath;
        Resource child;
        if( newPath.isRelative() ) {
            child = getResource();
            lastPath = path;
        } else {
            lastPath = Path.root();
            child = host();
        }
        for( String p : newPath.getParts() ) {
            if( p.equals( ".." ) ) {
                lastPath = lastPath.getParent();
                child = resourceFactory.getResource( host, lastPath.toString() );
            } else if( p.equals( "." ) ) {
                // do nothing
            } else {                
                if( child instanceof CollectionResource ) {
                    CollectionResource col = (CollectionResource) child;
                    child = col.child( p );
                    lastPath = Path.path( lastPath, p );
                    if( child == null ) {
                        String s = "child " + p + " not found in folder " + lastPath;
                        return new Cursor( resourceFactory, host, lastPath, null, s);
                    }
                } else {
                    return new Cursor( resourceFactory, host, lastPath, child, "Not a folder: " + lastPath);
                }
            }
        }
        return new Cursor(resourceFactory, host, lastPath, child, null);
    }

    /**
     * Attempt to search. If the search cannot be performed the getMessage()
     * property of this cursor is populated with the reason.
     *
     * @param sPattern
     * @return - null if couldnt search. a list which might contain more then zero
     * elements
     */
    public List<Resource> childrenWithFilter(String sPattern) {
        log.debug("findWithWildCard");
        Resource r = getResource();
        if(  r == null ){
            msg = "no current resource for path: " + path;
            return null;
        }
        if( !(r instanceof CollectionResource) ) {
            log.debug( "resource is not a collectionresource. is a: " + r.getClass());
            msg = "not a folder " + path;
            return null;
        }
        CollectionResource cur = (CollectionResource) r;
        Pattern pattern = null;
        try {
            log.debug("findWithWildCard: compiling " + sPattern);
            pattern = Pattern.compile(sPattern);
        } catch (Exception e) {
            msg = "Couldnt compile regular expression: " + sPattern;
            return null;
        }
        List<Resource> list = new ArrayList<Resource>();
        for (Resource res : cur.getChildren()) {
            Matcher m = pattern.matcher(res.getName());
            if (m.matches()) {
                log.debug("findWithWildCard: matches: " + res.getName());
                list.add(res);
            } else {
                log.debug("findWithWildCard: does not match: " + res.getName());
            }
        }
        return list;
    }

    public String getHost() {
        return host;
    }
}
