
package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import java.util.List;

public class HelpFactory extends AbstractConsoleCommandFactory {

    @Override
    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth) {
        return new Help(args, host, currentDir, consoleResourceFactory);
    }

    @Override
    public String[] getCommandNames() {
        return new String[]{"help"};
    }

    @Override
    public String getDescription() {
        return "Help. Display all commands";
    }

    

}
