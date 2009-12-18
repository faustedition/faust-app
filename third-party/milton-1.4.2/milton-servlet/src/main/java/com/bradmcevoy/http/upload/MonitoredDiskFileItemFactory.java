/* Licence:
 *   Use this however/wherever you like, just don't blame me if it breaks anything.
 *
 * Credit:
 *   If you're nice, you'll leave this bit:
 *
 *   Class by Pierre-Alexandre Losson -- http://www.telio.be/blog
 *   email : plosson@users.sourceforge.net
 */
package com.bradmcevoy.http.upload;

import org.apache.commons.fileupload.FileItem;
import org.apache.commons.fileupload.disk.DiskFileItemFactory;

import java.io.File;

public class MonitoredDiskFileItemFactory extends DiskFileItemFactory {
    private OutputStreamListener listener = null;
    
    public MonitoredDiskFileItemFactory(OutputStreamListener listener) {
        super();
        this.listener = listener;
    }
    
    public MonitoredDiskFileItemFactory(int sizeThreshold, File repository, OutputStreamListener listener) {
        super(sizeThreshold, repository);
        this.listener = listener;
    }
    
    public FileItem createItem(String fieldName, String contentType, boolean isFormField, String fileName) {
        return new MonitoredDiskFileItem(fieldName, contentType, isFormField, fileName, getSizeThreshold(), getRepository(), listener);
    }
}
