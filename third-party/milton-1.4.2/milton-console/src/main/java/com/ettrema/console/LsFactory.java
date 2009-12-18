package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import java.util.List;

public class LsFactory extends AbstractConsoleCommandFactory {

    @Override
    public String[] getCommandNames() {
        return new String[]{"ls"};
    }

    @Override
    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Ls( args, host, currentDir, consoleResourceFactory, new DefaultLinkGenerator( consoleResourceFactory.contextPath ) );
    }

    @Override
    public String getDescription() {
        return "List. List contents of the current or a specified directory";
    }
}
