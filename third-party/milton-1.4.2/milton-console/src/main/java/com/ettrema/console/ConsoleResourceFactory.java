package com.ettrema.console;


import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import com.bradmcevoy.http.SimpleResource;
import com.bradmcevoy.io.ReadingException;
import com.bradmcevoy.io.StreamUtils;
import com.bradmcevoy.io.WritingException;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;



/**
 *
 */
public class ConsoleResourceFactory implements ResourceFactory {

    private static final Logger log = LoggerFactory.getLogger(ConsoleResourceFactory.class);

    final ResourceFactory wrappedFactory;
    final String consolePath;
    final String consoleName;
    final String contextPath;
    final String secureResourcePath;
    final Date modDate;
    final Map<String,ConsoleCommandFactory> mapOfFactories;
    final List<ConsoleCommandFactory> factories;
    final String consolePageContent;
    final String dojoJsContent;

    public ConsoleResourceFactory(ResourceFactory wrappedFactory, String consolePath, String secureResourcePath, List<ConsoleCommandFactory> factories,String contextPath) {
        this.factories = factories;
        this.wrappedFactory = wrappedFactory;
        this.consolePath = consolePath;
        this.contextPath = contextPath;
        this.consoleName = consolePath.substring(consolePath.lastIndexOf("/"));
        this.secureResourcePath = secureResourcePath;
        this.modDate = new Date();
        mapOfFactories = new ConcurrentHashMap<String, ConsoleCommandFactory>();
        for( ConsoleCommandFactory f : factories ) {
            for (String cmdName : f.getCommandNames()) {
                log.debug("Console Command Factory: " + cmdName + " - " + f.getClass());
                f.setConsoleResourceFactory(this);
                mapOfFactories.put(cmdName, f);
            }
        }
        String s = loadContent("/com/ettrema/console/console.html");
        s = s.replace("CONSOLE_PATH", consolePath);
        this.consolePageContent = loadContent("console.html");
        this.dojoJsContent = loadContent("dojo.js");
    }

    public Resource getResource(String host, String path) {
        log.debug("path: " + path + " ::::::::::: consolePath:" + consolePath);
        if( path.startsWith(consolePath)) {
            path = stripConsolePath(path, consolePath);
            Resource secureResource = wrappedFactory.getResource(host, secureResourcePath);
            log.debug("checking: " + path);
            if( path.endsWith("index.html")) {
                return new SimpleResource("index.html", modDate, consolePageContent.getBytes(), "text/html", "console", null, secureResource);
            } else if( path.endsWith("dojo.js")) {
                return new SimpleResource("dojo.js", modDate, dojoJsContent.getBytes(), "text/html", "console", null, secureResource);
            } else if(  path.endsWith("console.json")) {                
                return new Console(host, wrappedFactory, consoleName, secureResource, modDate, mapOfFactories);
            } else {
                return null; // 4o4
            }
        } else {
            log.debug("not a console path");
            return wrappedFactory.getResource(host, path);
        }
    }

    public String getSupportedLevels() {
        return wrappedFactory.getSupportedLevels();
    }

    private String loadContent(String name) {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        InputStream in = Console.class.getResourceAsStream(name);
        if( in == null ) throw new RuntimeException("Couldnt find resource: " + name);
        try {
            StreamUtils.readTo(in, out);
        } catch (ReadingException ex) {
            throw new RuntimeException(ex);
        } catch (WritingException ex) {
            throw new RuntimeException(ex);
        }
        return  out.toString();
    }

    static String stripConsolePath( String path, String consolePath ) {
        return path.substring( consolePath.length() );
    }
}
