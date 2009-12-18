package com.bradmcevoy.http;
import org.apache.commons.codec.binary.Base64;



public class Auth {
    
    private Object tag;
    
    public enum Scheme {
        BASIC
    };
    
    public final Scheme scheme;
    public final String user;
    public final String password;
    
    public Auth(String sAuth) {
        int pos = sAuth.indexOf(" ");
        String schemeCode;
        String enc;
        if( pos >= 0 ) {
            schemeCode = sAuth.substring(0,pos);
            scheme = Scheme.valueOf(schemeCode.toUpperCase());
            enc = sAuth.substring(pos+1);
        } else {
            // assume basic
            scheme = Scheme.BASIC;
            enc = sAuth;
        }
        byte[] bytes = Base64.decodeBase64( enc.getBytes() );
        String s = new String(bytes);
        pos = s.indexOf(":");
        if( pos >= 0 ) {
            user = s.substring(0,pos);
            password = s.substring(pos+1);
        } else {
            user = s;
            password = null;
        }
    }
    
    public Auth(String user, Object userTag) {
        this.scheme = Scheme.BASIC;
        this.user = user;
        this.password = null;
        this.tag = userTag;
    }

    public String getUser() {
        return user;
    }

    void setTag(Object authTag) {
        tag = authTag;
    }

    public Object getTag() {
        return tag;
    }           
}
