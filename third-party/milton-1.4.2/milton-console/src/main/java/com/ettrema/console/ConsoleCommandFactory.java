
package com.ettrema.console;

import com.bradmcevoy.http.Auth;
import java.util.List;

public interface ConsoleCommandFactory {

    public ConsoleCommand create(List<String> args, String host, String currentDir, Auth auth);
    public String[] getCommandNames();
    public String getDescription();

    /**
     * Called on initialisation to give this factory to a reference to the 
     * consoleresourcefactory managing it
     * 
     * @param crf
     */
    public void setConsoleResourceFactory(ConsoleResourceFactory crf);
    
}
