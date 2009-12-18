package com.ettrema.console;

import com.bradmcevoy.common.Path;
import com.bradmcevoy.http.Resource;

/**
 *
 * @author brad
 */
public class DefaultLinkGenerator implements LinkGenerator{
    String contextPath;

    public DefaultLinkGenerator( String contextPath ) {
        this.contextPath = contextPath;
    }


    public String link(Path parentPath, Resource r) {
        StringBuffer sb = new StringBuffer();
        String href = appendContext(parentPath).append( '/').append( r.getName()).toString();
        sb.append("<a href='").append(href).append("'>").append(r.getName()).append("</a>");
        return sb.toString();
    }

    private StringBuffer appendContext( Path parentPath ) {
        StringBuffer sb = new StringBuffer();
        if( contextPath != null && contextPath.length() > 0 ) {
            sb.append( '/').append( contextPath);
        }
        sb.append( parentPath.toString());
        return sb;
    }

}
