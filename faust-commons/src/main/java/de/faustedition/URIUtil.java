package de.faustedition;

import java.net.URI;
import java.net.URISyntaxException;

public class URIUtil {
	private static final String FAUST_SCHEME = "faust";

	public static URI create(String authority, String path) {
		try {
			return new URI(FAUST_SCHEME, authority, path, null, null);
		} catch (URISyntaxException e) {
			throw ErrorUtil.fatal(e, "Syntax error while constructing URI for %s/%s'", authority, path);
		}
	}

	public static URI parse(String uriStr) throws URISyntaxException {
		URI uri = URI.create(uriStr);
		if (!FAUST_SCHEME.equals(uri.getScheme())) {
			throw new URISyntaxException(uriStr, "Not in faust scheme");
		}
		return uri;
		
	}
}
