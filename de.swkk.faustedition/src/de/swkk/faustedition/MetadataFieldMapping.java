package de.swkk.faustedition;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;

@Component
public class MetadataFieldMapping extends HashMap<String, String> implements InitializingBean {

	private static final long serialVersionUID = -7944897391198432284L;

	private Resource mappingDefinitionResource = new ClassPathResource("/weimar_metadata_field_mapping.xml");

	public void afterPropertiesSet() throws Exception {
		LoggingUtil.info("Initializing metadata field mapping");
		parse(mappingDefinitionResource);
	}

	protected void parse(Resource resource) throws SAXException, IOException {
		clear();
		XMLUtil.parse(resource.getInputStream(), new DefaultHandler() {

			@Override
			public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
				if ("field-mapping".equalsIgnoreCase(qName)) {
					String allegroId = attributes.getValue("allegroId");
					String metadataField = attributes.getValue("metadataField");

					if (allegroId == null || metadataField == null) {
						throw new SAXException("Incomplete field mapping");
					}

					put(allegroId, metadataField);
				}
			}
		});
	}
}
