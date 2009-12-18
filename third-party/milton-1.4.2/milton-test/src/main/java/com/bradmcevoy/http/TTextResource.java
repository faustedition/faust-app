package com.bradmcevoy.http;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.util.Date;
import java.util.Map;

/**
 *
 */
public class TTextResource extends TResource implements PostableResource, ReplaceableResource {
    private static org.apache.log4j.Logger log = org.apache.log4j.Logger.getLogger(TTextResource.class);

    private static final String START_CONTENT = "<textarea name=\"text\" cols=\"60\" rows=\"20\">";
    private static final String END_CONTENT = "</textarea>";

    private String text;

    public TTextResource(TFolderResource parent, String name, String text) {
        super(parent,name);
        this.text = text;
    }

    @Override
    protected Object clone(TFolderResource newParent) {
        return new TTextResource(newParent, name, text);
    }

    public String getContentType(String accept) {
        return Response.ContentType.HTTP.toString();
    }

    public void sendContent(OutputStream out, Range range, Map<String, String> params, String contentType) throws IOException {
        PrintWriter printer = new PrintWriter(out,true);
        sendContentStart(printer);
        sendContentMiddle(printer);
        sendContentFinish(printer);
    }

    protected  void sendContentMiddle(final PrintWriter printer) {
        print(printer, "<form method='post' action='" + this.getHref() + "'>");
        print(printer,"<fieldset>");
        print(printer,"<input type='text' name='name' value='" + this.getName() + "'/>");
        print(printer,"<br/>");
        printer.print(START_CONTENT);
        print(printer, text);
        printer.print(END_CONTENT);
        print(printer,"<br/>");
        printer.print("<input type='submit' />");
        print(printer,"</fieldset>");
        print(printer,"</form>");
    }

    protected void sendContentFinish(final PrintWriter printer) {
        printer.print("</body></html>");
        printer.flush();
    }

    protected void sendContentStart(final PrintWriter printer) {
        printer.print("<html>");
        printer.print("<head>");
        printer.print("<title>page: " + this.getName() +  "</title>");
        printer.print("</head>");
        printer.print("<body>");
        printer.print("<h1>" + getName() + "</h1>");
        sendContentMenu(printer);
    }

    protected void sendContentMenu(final PrintWriter printer) {
        printer.print("<ul>");
        for( TResource r : parent.children ) {
            printer.print("<li><a href='" + r.getHref() + "'>" + r.getName() + "</a></li>");
        }
        printer.print("</ul>");
    }

    public String processForm(Map<String, String> parameters, Map<String, FileItem> files) {
        log.debug("processForm: " + parameters.size());
        for( String nm : parameters.keySet() ) {
            log.debug(" - param: " + nm);
        }
        String newName = parameters.get("name");
        if( newName != null ) {
            this.name = newName;
        }
        String newContent = parameters.get("text");
        this.text = newContent;
        this.modDate = new Date();
        return null;
    }

    public void replaceContent(InputStream in, Long length) {
        try {
            String newContent = TFolderResource.readStream(in).toString();
            int pos = newContent.indexOf(START_CONTENT);
            if( pos >= 0 ) {
                newContent = newContent.substring(pos + START_CONTENT.length());
            }
            pos = newContent.indexOf(END_CONTENT);
            if( pos >= 0 ) {
                newContent = newContent.substring(0, pos);
            }
            log.debug("new content: " + newContent);
            this.text = newContent;
        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }

    }
}
