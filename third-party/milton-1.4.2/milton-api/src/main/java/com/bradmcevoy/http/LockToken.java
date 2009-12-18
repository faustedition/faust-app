
package com.bradmcevoy.http;

import java.util.Date;

public class LockToken {

    /**
     * the date/time that this lock was created or last refreshed
     */
    private Date from;

    public String tokenId;
    public LockInfo info;
    public LockTimeout timeout;

    public LockToken() {
        from = new Date();
    }

    public LockToken(String tokenId, LockInfo info, LockTimeout timeout) {
        from = new Date();
        this.tokenId = tokenId;
        this.info = info;
        this.timeout = timeout;
    }

    public Date getFrom() {
        return from;
    }

    public void setFrom(Date from) {
        this.from = from;
    }

    public boolean isExpired() {
        long secondsDif = dateDiffSeconds(new Date(), from);
        return ( secondsDif > timeout.getSeconds());
    }

    private long dateDiffSeconds(Date dt1, Date dt2) {
        return (dt1.getTime() - dt2.getTime()) / 1000;

    }
}
