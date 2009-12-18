
package com.ettrema.console;

public class Result {
    private String dir;
    private String output;
    private String redirect;

    public Result(String dir, String output) {
        this.dir = dir;
        this.output = output;
    }

    public Result(String redirect) {
        this.dir = null;
        this.output = null;
        this.redirect = redirect;
    }

    public Result() {
    }

    
    
    public String getDir() {
        return dir;
    }

    public void setDir(String dir) {
        this.dir = dir;
    }

    public String getOutput() {
        return output;
    }

    public void setOutput(String output) {
        this.output = output;
    }

    @Override
    public String toString() {
        return dir + " :: " + output;
    }

    public String getRedirect() {
        return redirect;
    }

    public void setRedirect(String redirect) {
        this.redirect = redirect;
    }

    
    
}
