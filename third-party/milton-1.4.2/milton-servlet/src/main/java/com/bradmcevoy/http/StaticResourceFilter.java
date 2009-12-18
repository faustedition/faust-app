package com.bradmcevoy.http;

import java.io.File;
import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StaticResourceFilter implements Filter {
    
    private Logger log = LoggerFactory.getLogger(StaticResourceFilter.class);
    
    private FilterConfig filterConfig = null;
    
    public void doFilter(ServletRequest request, ServletResponse response,FilterChain chain)throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        String s = MiltonUtils.stripContext(req);
        log.debug("url: " + s);
        s = "WEB-INF/static/" + s;
        log.debug("check path: " + s);
        String path = filterConfig.getServletContext().getRealPath(s);
        log.debug("real path: " + path);
        File f = null; 
        if( path != null && path.length() > 0 ) {
            f = new File(path);
        }
        if( f != null && f.exists() && !f.isDirectory() ) {  // can't forward to a folder
            log.debug("static file exists. forwarding..");
            req.getRequestDispatcher(s).forward(request,response);
        } else {
            log.debug("static file does not exist, continuing chain..");
            chain.doFilter(request,response);
        }
    }
    
    
    public FilterConfig getFilterConfig() {
        return (this.filterConfig);
    }
    
    public void destroy() {
    }
    
    public void init(FilterConfig filterConfig) {
        this.filterConfig = filterConfig;
    }
    
}
