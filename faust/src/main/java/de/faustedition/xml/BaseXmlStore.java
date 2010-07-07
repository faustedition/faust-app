package de.faustedition.xml;

import java.net.URI;

import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

import de.faustedition.Log;

public abstract class BaseXmlStore implements XmlStore, InitializingBean {
	
	@Value("#{config['xmldb.base']}")
	private String baseUri;

	protected URI base;

	public void afterPropertiesSet() throws Exception {
		this.base = new URI(baseUri);
		Log.LOGGER.info("Initialized XML store with base URI: " + base.toString());
	}

	protected boolean isCollection(URI uri) {
		return StringUtils.isBlank(uri.getPath()) || uri.getPath().endsWith("/");
	}

	protected URI relativize(URI uri) {
		Assert.isTrue(!uri.isAbsolute() && (StringUtils.isBlank(uri.getPath()) || !uri.getPath().startsWith("/")), "Invalid URI");
		return base.resolve(uri);
	}
	
	public boolean isWitnessEncodingDocument(URI uri) {
		return uri.getPath().startsWith(XmlStore.WITNESS_BASE.getPath()) && "xml".equals(FilenameUtils.getExtension(uri.getPath()));
	}
	
	public boolean isDocumentEncodingDocument(URI uri) {
		if (!isWitnessEncodingDocument(uri)) {
			return false;
		}
		String uriPath = uri.getPath();
		String path = FilenameUtils.getPathNoEndSeparator(uriPath);
		String basename = FilenameUtils.getBaseName(uriPath);
		return !basename.equals(FilenameUtils.getName(path));
	}
	
	public boolean isTextEncodingDocument(URI uri) {
		if (!isWitnessEncodingDocument(uri)) {
			return false;
		}
		String uriPath = uri.getPath();
		String path = FilenameUtils.getPathNoEndSeparator(uriPath);
		String basename = FilenameUtils.getBaseName(uriPath);
		return basename.equals(FilenameUtils.getName(path));		
	}
}
