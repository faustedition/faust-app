package de.faustedition.xml;

import java.net.URI;

import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.util.Assert;

public abstract class BaseXmlStore implements XmlStore, InitializingBean {
	protected Logger LOG = LoggerFactory.getLogger(getClass());

	@Value("#{config['xmldb.base']}")
	private String baseUri;

	protected URI base;

	public void afterPropertiesSet() throws Exception {
		this.base = new URI(baseUri);
		LOG.info("Initialized XML store with base URI: " + base.toString());
	}

	protected boolean isCollection(URI uri) {
		return StringUtils.isBlank(uri.getPath()) || uri.getPath().endsWith("/");
	}

	protected URI relativize(URI uri) {
		Assert.isTrue(!uri.isAbsolute() && (StringUtils.isBlank(uri.getPath()) || !uri.getPath().startsWith("/")), "Invalid URI");
		return base.resolve(uri);
	}

}
