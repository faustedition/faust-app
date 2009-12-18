package com.bradmcevoy.http;

import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.TimeZone;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractResponse implements Response {

    private static final Logger log = LoggerFactory.getLogger(AbstractResponse.class);


    protected DateFormat hdf;
    protected Long contentLength;
    

    public AbstractResponse() {
        hdf = new SimpleDateFormat(DateUtils.PATTERN_RESPONSE_HEADER);
        hdf.setTimeZone(TimeZone.getTimeZone("GMT"));
    }



    public void setResponseHeader(Response.Header header, String value) {
        //log.debug("setResponseHeader: " + header.code + " - " + value);
        setNonStandardHeader(header.code, value);
    }

    public String getResponseHeader(Response.Header header) {
        return getNonStandardHeader(header.code);
    }

    public void setContentEncodingHeader(ContentEncoding encoding) {
        setResponseHeader(Response.Header.CONTENT_ENCODING, encoding.code);
    }

    public Long getContentLength() {
        return contentLength;
    }
    
    
    public void setDateHeader(Date date) {
        setAnyDateHeader(Header.DATE, date);
    }

    public void setAuthenticateHeader(String realm) {
        setResponseHeader(Header.WWW_AUTHENTICATE, "Basic realm=\"" + realm + "\"");
    }

    public void setContentRangeHeader(long start, long finish, Long totalLength) {
        String l = totalLength == null ? "*" : totalLength.toString();

        String s = null;
        if( finish > -1)
        {
        s = "bytes " + start + "-" + finish + "/" + l;
        }
        else
        {
        	long wrotetill = totalLength.longValue() - 1;
        	//The end position starts counting at zero. So subtract 1
            s = "bytes " + start + "-" + wrotetill + "/" + l;
        }
 
        
        setResponseHeader(Header.CONTENT_RANGE, s);
    }

    public void setContentLengthHeader(Long totalLength) {
        String s = totalLength == null ? "" : totalLength.toString();
        setResponseHeader(Header.CONTENT_LENGTH, s);
        this.contentLength = totalLength;
        
    }

    public void setContentTypeHeader(String type) {
        setResponseHeader(Header.CONTENT_TYPE, type);
    }

    public String getContentTypeHeader() {
        return getResponseHeader(Header.CONTENT_TYPE);
    }

    public void setCacheControlMaxAgeHeader(Long delta) {
        if( delta != null ) {
            setResponseHeader(Header.CACHE_CONTROL, CacheControlResponse.MAX_AGE.code + "=" + delta);
        } else {
            setResponseHeader(Header.CACHE_CONTROL, CacheControlResponse.NO_CACHE.code);
        }
    }

    public void setCacheControlPrivateMaxAgeHeader(Long delta) {
        if( delta != null ) {
            setResponseHeader(Header.CACHE_CONTROL, CacheControlResponse.PRIVATE.code + " " + CacheControlResponse.MAX_AGE.code + "=" + delta);
        } else {
            setResponseHeader(Header.CACHE_CONTROL, CacheControlResponse.PRIVATE.code);
        }
    }



    public void setExpiresHeader(Date expiresAt) {
        if (expiresAt == null) {
            setResponseHeader(Header.EXPIRES, null);
        } else {
            setAnyDateHeader(Header.EXPIRES, expiresAt);
        }
    }

    public void setEtag(String uniqueId) {
        setResponseHeader(Header.ETAG, uniqueId);
    }

    public void setLastModifiedHeader(Date date) {
        setAnyDateHeader(Header.LAST_MODIFIED, date);
    }

    public void setCacheControlNoCacheHeader() {
        setResponseHeader(Header.CACHE_CONTROL, CacheControlResponse.NO_CACHE.code);
    }

    public void setLocationHeader(String redirectUrl) {
        setResponseHeader(Header.LOCATION, redirectUrl);
    }

    public void setAllowHeader(List<Request.Method> methodsAllowed) {
        if (methodsAllowed == null || methodsAllowed.size() == 0)
            return;
        StringBuffer sb = null;
        for (Request.Method m : methodsAllowed) {
            if (sb == null) {
                sb = new StringBuffer();
            } else {
                sb.append(",");
            }
            sb.append(m.code);
        }
        setResponseHeader(Header.ALLOW, sb.toString());
    }

    public void setLockTokenHeader(String s) {
        setResponseHeader(Header.LOCK_TOKEN, s);
    }

    public void setDavHeader(String supportedLevels) {
        setResponseHeader(Header.DAV, supportedLevels);
    }

    public void close() {
    }

    public void sendRedirect(String url) {
        setStatus(Response.Status.SC_MOVED_TEMPORARILY);
        setLocationHeader( url );        
    }

    public void write(String s) {
        try {
            this.getOutputStream().write(s.getBytes());
        } catch (IOException ex) {
            log.warn("Exception writing to output. Probably client closed connection", ex);
        }
    }    

    protected void setAnyDateHeader(Header name, Date date) {
        if (date == null)
            return;
        String fmt = hdf.format(date);
        setResponseHeader(name, fmt);

    }
}
