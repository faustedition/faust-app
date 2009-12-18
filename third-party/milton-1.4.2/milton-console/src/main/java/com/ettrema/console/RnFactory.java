package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import java.util.List;

public class RnFactory extends AbstractConsoleCommandFactory {

    @Override
    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Rn( args, host, currentDir, consoleResourceFactory );
    }

    @Override
    public String[] getCommandNames() {
        return new String[]{"rn", "rename"};
    }

    @Override
    public String getDescription() {
        return "Rename.";
    }
}
