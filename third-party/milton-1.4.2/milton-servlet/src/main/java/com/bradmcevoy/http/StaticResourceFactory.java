package com.bradmcevoy.http;

import java.io.File;

public class StaticResourceFactory implements ResourceFactory, Initable{
    
    private ApplicationConfig config;

    public void init(ApplicationConfig config, HttpManager manager) {
        this.config = config;
    }
    
    public Resource getResource(String host, String url) {
        if( config == null ) throw new RuntimeException("ResourceFactory was not configured. ApplicationConfig is null");
        if( config.servletContext == null ) throw new NullPointerException("config.servletContext is null");
        String path = "WEB-INF/static" + url;
        path = config.servletContext.getRealPath(path);
        File file = new File(path);
        if( file.exists() && !file.isDirectory() ) {
            String contentType = MiltonUtils.getContentType(config.servletContext, file.getName());
            return new StaticResource(file,url, contentType);
        } else {
            return null;
        }
    }

    public void destroy(HttpManager manager) {
    }

    public String getSupportedLevels() {
        return "1,2";
    }
    
    
}
