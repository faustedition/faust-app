package de.faustedition;

import java.net.URI;
import java.net.URISyntaxException;

import com.google.common.base.Preconditions;

public class FaustURI implements Comparable<FaustURI> {
    public enum Authority {
        XML, FACSIMILE
    }

    public static final String FAUST_NS_URI = "http://www.faustedition.net/ns";
    private static final String FAUST_SCHEME = "faust";

    private final URI uri;

    public FaustURI(Authority authority, String path) {
        try {
            this.uri = new URI(FAUST_SCHEME, authority.name().toLowerCase(), path, null);
        } catch (URISyntaxException e) {
            throw new IllegalArgumentException(path);
        }
    }

    protected FaustURI(URI uri) {
        this.uri = uri;
    }

    public static FaustURI parse(String uriStr) throws IllegalArgumentException, NullPointerException {
        final URI uri = URI.create(uriStr);
        Preconditions.checkArgument(FAUST_SCHEME.equals(uri.getScheme()));
        Preconditions.checkNotNull(uri.getPath());
        Preconditions.checkNotNull(uri.getAuthority());
        Preconditions.checkNotNull(Authority.valueOf(uri.getAuthority().toUpperCase()));
        return new FaustURI(uri);

    }

    public Authority getAuthority() {
        return Authority.valueOf(uri.getAuthority().toUpperCase());
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
}
