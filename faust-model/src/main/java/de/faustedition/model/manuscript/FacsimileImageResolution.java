/**
 * 
 */
package de.faustedition.model.manuscript;

public enum FacsimileImageResolution {
	THUMB("_thumb.jpg", "image/jpeg"), LOW(".jpg", "image/jpeg"), HIGH(".tif", "image/tiff");

	private String suffix;
	private String mimeType;

	private FacsimileImageResolution(String suffix, String mimeType) {
		this.suffix = suffix;
		this.mimeType = mimeType;
	}

	public String getSuffix() {
		return suffix;
	}

	public boolean matches(String str) {
		return str.endsWith(suffix);
	}

	public String getMimeType() {
		return mimeType;
	}
}