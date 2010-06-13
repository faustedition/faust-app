package de.faustedition.xml;

import java.net.URI;

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

}
