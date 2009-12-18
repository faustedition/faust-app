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

public class UploadListener implements OutputStreamListener {

    private int totalBytesRead = 0;
    private int totalFiles = -1;
    
    public UploadListener( ) {
//        totalToRead = request.getContentLength();
//        this.startTime = System.currentTimeMillis();
    }
    
    public void start() {
        totalFiles ++;
        updateUploadInfo("start");
    }
    
    public void bytesRead(int bytesRead) {
        totalBytesRead = totalBytesRead + bytesRead;
        updateUploadInfo("progress");        
    }
    
    public void error(String message) {
        updateUploadInfo("error");
    }
    
    public void done() {
        updateUploadInfo("done");
    }
    
    private void updateUploadInfo(String status) {
//        long delta = (System.currentTimeMillis() - startTime) / 1000;
//        request.getSession().setAttribute("uploadInfo", new UploadInfo(totalFiles, totalToRead, totalBytesRead,delta,status));
    }
    
}
