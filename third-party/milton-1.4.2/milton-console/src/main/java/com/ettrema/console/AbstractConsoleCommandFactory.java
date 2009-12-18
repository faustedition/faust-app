package com.ettrema.console;

/**
 *
 * @author brad
 */
public abstract  class AbstractConsoleCommandFactory implements ConsoleCommandFactory{
    ConsoleResourceFactory consoleResourceFactory;


    public final void setConsoleResourceFactory( ConsoleResourceFactory crf ) {
        this.consoleResourceFactory = crf;
    }

}
