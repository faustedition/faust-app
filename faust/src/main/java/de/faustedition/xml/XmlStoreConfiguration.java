package de.faustedition.xml;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class XmlStoreConfiguration {
	@Value("#{config['xmldb.impl']}")
	private String xmlStoreClazz;

	@Bean
	public XmlStore xmlStore() throws Exception {
		return (XmlStore) Class.forName(xmlStoreClazz).newInstance();
	}

}
