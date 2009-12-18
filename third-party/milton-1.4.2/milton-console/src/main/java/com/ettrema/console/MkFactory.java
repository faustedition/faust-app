
package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import java.util.List;

public class MkFactory extends AbstractConsoleCommandFactory{

    @Override
    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth) {
        return new Mk(args,host,currentDir,consoleResourceFactory);
    }

    public String[] getCommandNames() {
        return new String[] {"mk","make","new"};
    }

    public String getDescription() {
        return "Create an item of a given type and name. Usage mk com.blah.File newname, or mk templatename newname";
    }
    
}
