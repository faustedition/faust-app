package de.faustedition;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayDeque;
import java.util.Arrays;
import java.util.Deque;

import com.google.common.base.Preconditions;

public class FaustURI implements Comparable<FaustURI> {
	public static final String FAUST_SCHEME = "faust";

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

	public boolean isWitnessEncodingDocument() {
		final String path = uri.getPath();
		final int extensionIndex = path.lastIndexOf(".");
		return extensionIndex >= 0 && path.startsWith("/transcript/") && "xml".equals(path.substring(extensionIndex + 1));
	}

	public boolean isDocumentEncodingDocument() {
		return isWitnessEncodingDocument() && !isTextEncodingDocument();
	}

	public boolean isTextEncodingDocument() {
		if (!isWitnessEncodingDocument()) {
			return false;
		}
		final String uriPath = uri.getPath().replaceAll("/+", "/");

		final int basenameStart = uriPath.lastIndexOf("/");
		if (basenameStart < 0) {
			return false;
		}

		final int folderNameStart = uriPath.lastIndexOf("/", basenameStart - 1);
		if (folderNameStart < 0) {
			return false;
		}

		final int basenameEnd = uriPath.lastIndexOf(".");
		if (basenameEnd < 0 || basenameEnd < basenameStart || !uriPath.substring(basenameEnd).equalsIgnoreCase(".xml")) {
			return false;
		}

		final String basename = uriPath.substring(basenameStart + 1, basenameEnd);
		final String folderName = uriPath.substring(folderNameStart + 1, basenameStart);
		return basename.equalsIgnoreCase(folderName);
	}

	public static Deque<String> toPathDeque(String path) {
		return new ArrayDeque<String>(Arrays.asList(path.replaceAll("^/+", "").replaceAll("/+$", "").split("/+")));
	}
}
