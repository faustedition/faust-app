package de.faustedition.util;

import java.net.URI;
import java.net.URISyntaxException;

public class URIUtil {

	private static final String FACSIMILE_AUTHORITY = "facsimile";
	private static final String FAUST_SCHEME = "faust";

	public static URI createFacsimileURI(String facsimilePath) {
		try {
			return new URI(FAUST_SCHEME, FACSIMILE_AUTHORITY, "/" + facsimilePath, null, null);
		} catch (URISyntaxException e) {
			throw ErrorUtil.fatal(e, "Syntax error while constructing facsimile URI for '" + facsimilePath + "'");
		}
	}
}
