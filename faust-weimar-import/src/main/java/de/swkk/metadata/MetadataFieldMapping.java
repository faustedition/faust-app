package de.swkk.metadata;

import java.io.IOException;
import java.util.HashMap;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Required;
import org.springframework.core.io.Resource;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.faustedition.util.LoggingUtil;
import de.faustedition.util.XMLUtil;

public class MetadataFieldMapping extends HashMap<String, String> implements InitializingBean {
	private Resource mappingDefinitionResource;

	@Required
	public void setMappingDefinitionResource(Resource mappingDefinitionResource) {
		this.mappingDefinitionResource = mappingDefinitionResource;
	}

	public void afterPropertiesSet() throws Exception {
		LoggingUtil.LOG.info("Initializing metadata field mapping");
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
