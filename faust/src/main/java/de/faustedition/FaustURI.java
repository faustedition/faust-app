package de.faustedition;

import java.net.URI;
import java.net.URISyntaxException;

import com.google.common.base.Preconditions;

public class FaustURI implements Comparable<FaustURI> {
    private static final String FAUST_SCHEME = "faust";

    private URI uri;

    public FaustURI(FaustAuthority authority, String path) {
        try {
            setURI(new URI(FAUST_SCHEME, authority.name().toLowerCase(), path, null));
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(e);
        }
    }

    public FaustURI(URI uri) {
        setURI(uri);
    }

    protected void setURI(URI uri) {
        Preconditions.checkArgument(FAUST_SCHEME.equals(uri.getScheme()));
        Preconditions.checkNotNull(uri.getPath());
        Preconditions.checkNotNull(uri.getAuthority());
        Preconditions.checkNotNull(FaustAuthority.valueOf(uri.getAuthority().toUpperCase()));
        this.uri = uri;
    }

    public static FaustURI parse(String uriStr) {
        return new FaustURI(URI.create(uriStr));

    }

    public FaustAuthority getAuthority() {
        return FaustAuthority.valueOf(uri.getAuthority().toUpperCase());
    }

    public String getPath() {
        return uri.getPath();
    }

    @Override
    public boolean equals(Object obj) {
        return uri.equals(obj);
    }

    @Override
    public int hashCode() {
        return uri.hashCode();
    }

    @Override
    public String toString() {
        return uri.toString();
    }

    @Override
    public int compareTo(FaustURI o) {
        return uri.compareTo(o.uri);
    }

    public FaustURI resolve(String relative) {        
        return new FaustURI(this.uri.resolve(relative));
    }
}
