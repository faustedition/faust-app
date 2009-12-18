package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import java.util.List;

public class CpFactory extends AbstractConsoleCommandFactory {

    @Override
    public ConsoleCommand create( List<String> args, String host, String currentDir, Auth auth ) {
        return new Cp( args, host, currentDir, consoleResourceFactory );
    }

    @Override
    public String[] getCommandNames() {
        return new String[]{"cp", "copy"};
    }

    @Override
    public String getDescription() {
        return "Copies a file or folder to a destination file or folder";
    }
}
