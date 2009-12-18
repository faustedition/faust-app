package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import com.bradmcevoy.http.FileItem;
import com.bradmcevoy.http.GetableResource;
import com.bradmcevoy.http.PostableResource;
import com.bradmcevoy.http.Range;
import com.bradmcevoy.http.Request;
import com.bradmcevoy.http.Request.Method;
import com.bradmcevoy.http.Resource;
import com.bradmcevoy.http.ResourceFactory;
import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import net.sf.json.JSON;
import net.sf.json.JSONSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public class Console implements GetableResource, PostableResource {

    private static final Logger log = LoggerFactory.getLogger(Console.class);

    final String host;
    final ResourceFactory wrappedFactory;
    final String name;
    
    final Date modDate;
    final Resource secureResource;
    final Map<String,ConsoleCommandFactory> mapOfFactories;

    transient Auth auth;
    transient Result result;

    public Console(String host, final ResourceFactory wrappedFactory, String name, Resource secureResource, Date modDate, Map<String,ConsoleCommandFactory> mapOfFactories) {
        this.host = host;
        this.wrappedFactory = wrappedFactory;
        this.name = name;
        this.secureResource = secureResource;
        this.modDate = modDate;
        this.mapOfFactories = mapOfFactories;
    }

    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        JSON json = JSONSerializer.toJSON(result);
        PrintWriter writer = new PrintWriter(out);
        json.write(writer);
        writer.flush();
    }

    public Long getMaxAgeSeconds(Auth auth) {
        return null;
    }

    public String getContentType(String accepts) {
        if( result != null ) {
            return "text/plain";
        } else {
            return "text/html";
        }
    }

    public Long getContentLength() {
        return null;
    }

    public String getUniqueId() {
        return null;
    }

    public String getName() {
        return name;
    }

    public Object authenticate(String user, String password) {
        return secureResource.authenticate(user, password);

    }

    public boolean authorise(Request request, Method method, Auth auth) {
        this.auth = auth;
        return secureResource.authorise(request, method, auth);
    }

    public String getRealm() {
        return secureResource.getRealm();
    }

    public Date getModifiedDate() {
        return modDate;
    }

    public String checkRedirect(Request request) {
        return null;
    }

    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) {
        for(String s : parameters.keySet() ) {
            log.debug("param: "  + s);
        }
        String sCmd = parameters.get("cmd");
        log.debug("command string: " + sCmd);
        if( sCmd == null || sCmd.length() == 0 ) {
            result = new Result("/", "No command specified");
            return null;
        }
        String currentDir = parameters.get("currentDir");
        if( currentDir == null ) currentDir = "/";
        String[] arr = sCmd.split(" ");  // todo: this won't handle quoted arguments properly eg cp "a file" "another file"
        try {
            result = doCmd( currentDir, arr, host );
        } catch( Throwable e ) {
            String s = "";
            for( StackTraceElement el : e.getStackTrace() ) {
                s = s + el.getClassName() + "::" + el.getMethodName() + " (" + el.getLineNumber() + ") <br/>" ;
            }
            result = new Result( currentDir, "Exception prcessing command: " + e.getClass() + " - " + e.getMessage() + "<br/>" + s);
            log.error( sCmd,e);
        }
        if( result == null ) {
            result = new Result( currentDir, "The command did not return a result");
        }
        return result.getRedirect();  // allow results to cause a redirect
    }


    private Result doCmd(String currentDir, String[] arr, String host) {
        if (arr.length == 0) {
            return new Result(currentDir, "");
        }
        String sCmd = arr[0];
        List<String> args = new ArrayList<String>();
        for (int i = 1; i < arr.length; i++) {
            args.add(arr[i]);
        }
        return doCmd(currentDir, sCmd, args, host);
    }

    private Result doCmd(String currentDir, String sCmd, List<String> args, String host) {
        ConsoleCommand cmd = create(sCmd, args, host, currentDir);

        if (cmd == null) {
            log.debug("command not found: " + sCmd);
            return new Result(currentDir, "Unknown command: " + sCmd);
        }

        return cmd.execute();
    }

    ConsoleCommand create(String sCmd, List<String> args, String host, String currentDir) {
        ConsoleCommandFactory f = mapOfFactories.get(sCmd);
        if (f == null) {
            log.debug("no factory: " + sCmd);
            return null;
        }
        return f.create(args, host, currentDir, auth);
    }

}
