package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import java.util.List;

public class RmFactory extends AbstractConsoleCommandFactory {

    @Override
    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Rm( args, host, currentDir, consoleResourceFactory );
    }

    @Override
    public String[] getCommandNames() {
        return new String[]{"rm", "delete", "del"};
    }

    @Override
    public String getDescription() {
        return "Remove. Removes a file or folder by path or name, including regular expressions";
    }
}
