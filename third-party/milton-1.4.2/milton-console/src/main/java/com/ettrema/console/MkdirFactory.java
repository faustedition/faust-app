package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import java.util.List;

public class MkdirFactory extends AbstractConsoleCommandFactory {

    @Override
    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Mkdir( args, host, currentDir, consoleResourceFactory );
    }

    public String[] getCommandNames() {
        return new String[]{"mkdir"};
    }

    public String getDescription() {
        return "Make Directory";
    }
}
