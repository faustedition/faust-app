package com.bradmcevoy.http;

import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MultipleResourceFactory implements ResourceFactory, Initable {
    
    private Logger log = LoggerFactory.getLogger(MultipleResourceFactory.class);
    
    private final List<ResourceFactory> factories;

    public MultipleResourceFactory() {
        factories = new ArrayList<ResourceFactory>();
    }

    public MultipleResourceFactory( List<ResourceFactory> factories ) {
        this.factories = factories;
    }
        

    public Resource getResource(String host, String url) {
        log.debug( "getResource: " + url);
        for( ResourceFactory rf : factories ) {
            Resource r = rf.getResource(host,url);
            if( r != null ) {
                return r;
            }
        }
        log.debug("no resource factory supplied a resouce");
        return null;
    }

    public String getSupportedLevels() {
        String s = "1,2";
        for( ResourceFactory rf : factories ) {
            String s2 = rf.getSupportedLevels();
            if( s2.length() < s.length() ) return s2;
        }
        return s;
    }
    
    public void init(ApplicationConfig config, HttpManager manager) {        
        String sFactories = config.getInitParameter("resource.factory.multiple");
        init(sFactories, config, manager);
    }
     

    protected void init(String sFactories,ApplicationConfig config, HttpManager manager) {
        log.debug("init: " + sFactories );
        String[] arr = sFactories.split(",");
        for(String s : arr ) {
            createFactory(s,config,manager);
        }        
    }
    
    private void createFactory(String s,ApplicationConfig config, HttpManager manager) {
        log.debug("createFactory: " + s);
        Class c;
        try {
            c = Class.forName(s);
        } catch (ClassNotFoundException ex) {
            throw new RuntimeException(s,ex);
        }
        Object o;
        try {
            o = c.newInstance();
        } catch (IllegalAccessException ex) {
            throw new RuntimeException(s,ex);
        } catch (InstantiationException ex) {
            throw new RuntimeException(s,ex);
        }
        ResourceFactory rf = (ResourceFactory) o;
        if( rf instanceof Initable ) {
            Initable i = (Initable)rf;
            i.init(config,manager);
        }
        factories.add(rf);
    }

    public void destroy(HttpManager manager) {
        if( factories == null ) {
            log.warn("factories is null");
            return ;
        }
        for( ResourceFactory f : factories ) {
            if( f instanceof Initable ) {
                ((Initable)f).destroy(manager);
            }
        }
    }    
}
