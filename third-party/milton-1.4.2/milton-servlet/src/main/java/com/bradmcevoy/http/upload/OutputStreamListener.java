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

public interface OutputStreamListener
{
    public void start();
    public void bytesRead(int bytesRead);
    public void error(String message);
    public void done();
}
