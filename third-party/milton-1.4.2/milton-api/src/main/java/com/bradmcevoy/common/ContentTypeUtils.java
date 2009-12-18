package com.bradmcevoy.common;

import eu.medsea.mimeutil.MimeException;
import eu.medsea.mimeutil.MimeType;
import eu.medsea.mimeutil.MimeUtil;
import java.io.File;
import java.util.Collection;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author brad
 */
public class ContentTypeUtils {

    private static Logger log = LoggerFactory.getLogger(ContentTypeUtils.class);

    public static String findContentTypes( String name ) {
        Collection mimeTypes = MimeUtil.getMimeTypes( name );
        return buildContentTypeText(mimeTypes);
    }

    public static String findContentTypes( File file ) {
        Collection mimeTypes = null;
        try {
            mimeTypes = MimeUtil.getMimeTypes( file );
        } catch( MimeException e ) {
            log.warn( "exception retrieving content type for file: " + file.getAbsolutePath(),e);
            return "application/binary";
        }
        return buildContentTypeText(mimeTypes);
    }

    public static String findAcceptableContentType(String mime, String preferredList) {
        MimeType mt = MimeUtil.getPreferedMimeType(preferredList, mime);
        return mt.toString();

    }

    private static String buildContentTypeText( Collection mimeTypes ) {
        StringBuffer sb = null;
        for( Object o : mimeTypes ) {
            MimeType mt = (MimeType) o;
            if( sb == null ) {
                sb = new StringBuffer();
            } else {
                sb.append( "," );
            }
            sb.append( mt.toString() );
        }
        return sb.toString();
    }
}
