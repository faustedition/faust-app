
package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import java.util.List;

public class CdFactory implements ConsoleCommandFactory {

    ConsoleResourceFactory consoleResourceFactory;

    @Override
    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth) {
        return new Cd(args, host, currentDir, consoleResourceFactory);
    }

    @Override
    public String[] getCommandNames() {
        return new String[]{"cd"};
    }

    @Override
    public String getDescription() {
        return "Change Directory to a path, absolute or relative";
    }

    public void setConsoleResourceFactory(ConsoleResourceFactory crf) {
        this.consoleResourceFactory = crf;
    }
    
    

}
