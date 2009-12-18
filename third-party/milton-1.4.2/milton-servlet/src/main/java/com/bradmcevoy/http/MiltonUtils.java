package com.bradmcevoy.http;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.ServletContext;

public class MiltonUtils {
    public static String stripContext(HttpServletRequest req) {
        String s = req.getRequestURI();        
        String contextPath = req.getContextPath();        
        s = s.replaceFirst( contextPath  , "" );
        return s;
    }
    
    /**
     * 
     * @param context - context to look up mime associations with
     * @param fileName - name of a file to look for mime associations of
     * 
     * @return - a single content type spec
     */
    public static String getContentType(ServletContext context, String fileName) {
        return context.getMimeType(fileName);
    }
}
